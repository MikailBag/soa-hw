package auto

import (
	"context"
	"fmt"
	"math/rand"
	"time"

	"example.com/soa/client"
	"example.com/soa/common"
	"example.com/soa/pb"
)

type Bot struct {
	cs           client.ClientSet
	gameId       string
	myId         string
	watcher      *common.Watcher
	lastActivity uint32
}

func (b *Bot) waitStarted(ctx context.Context) error {
	var logged bool
	for {
		req := pb.GetRequest{
			RoomId:        b.gameId,
			ParticipantId: b.myId,
		}
		res, err := b.cs.Games.Get(ctx, &req)
		if err != nil {
			return fmt.Errorf("failed to get game: %w", err)
		}
		if res.Game.State != pb.Game_NOT_STARTED {
			return nil
		}
		if !logged {
			common.PrintGame(res.Game)
			fmt.Println("Waiting for game to start")
			logged = true
		}
		err = b.watcher.Wait(ctx, res.Game.Revision)
		if err != nil {
			return fmt.Errorf("watch failed: %w", err)
		}
	}
}

type actionOutcome int

const (
	madeAction actionOutcome = 1
	needWait   actionOutcome = 2
	stop       actionOutcome = 3
)

func pick(arr []string) (string, bool) {
	if len(arr) == 0 {
		return "", false
	}
	return arr[rand.Intn(len(arr))], true
}

func (b *Bot) findTargetForCitizenVote(game *pb.Game, myRole pb.Role) (string, bool) {
	targets := make([]string, 0)
	for _, p := range game.Participants {
		if p.State != pb.Participant_ALIVE {
			continue
		}
		if p.Id == b.myId {
			continue
		}
		if myRole == pb.Role_NORMAL || myRole == pb.Role_POLICEMAN {
			if p.Role == pb.Role_NORMAL || p.Role == pb.Role_POLICEMAN {
				continue
			}
		}
		if myRole == pb.Role_CRIMINAL || myRole == pb.Role_CRIMINAL_BOSS {
			if p.Role == pb.Role_CRIMINAL || p.Role == pb.Role_CRIMINAL_BOSS {
				continue
			}
		}
		targets = append(targets, p.Id)
	}
	return pick(targets)
}

func (b *Bot) findTargetForCriminalVote(game *pb.Game) (string, bool) {
	targets := make([]string, 0)
	for _, p := range game.Participants {
		if p.State != pb.Participant_ALIVE {
			continue
		}
		if p.Role == pb.Role_CRIMINAL || p.Role == pb.Role_CRIMINAL_BOSS {
			continue
		}
		targets = append(targets, p.Id)
	}
	return pick(targets)
}

func (b *Bot) findTargetForCheck(game *pb.Game) string {
	targets := make([]string, 0)
	for _, p := range game.Participants {
		if p.State != pb.Participant_ALIVE {
			continue
		}
		if p.Role != pb.Role_UNKNOWN {
			continue
		}
		targets = append(targets, p.Id)
	}
	res, found := pick(targets)
	if found {
		return res
	}

	return b.myId
}

func (b *Bot) action(ctx context.Context, game *pb.Game, myRole pb.Role) (actionOutcome, error) {
	if game.State == pb.Game_NOT_STARTED {
		panic("Invalid game state")
	}
	if game.State == pb.Game_FINISHED {
		return stop, nil
	}
	if b.lastActivity == common.MakeComparisonKey(game) {
		return needWait, nil
	}
	isCriminal := myRole == pb.Role_CRIMINAL || myRole == pb.Role_CRIMINAL_BOSS
	if game.GetDay() != nil {
		// vote as a citizen
		target, found := b.findTargetForCitizenVote(game, myRole)
		if !found {
			return stop, fmt.Errorf("failed to find vote target")
		}
		fmt.Printf("Voting to sentence %s\n", target)
		req := pb.CitizenVoteRequest{
			RoomId:        b.gameId,
			SuspectedId:   target,
			ParticipantId: b.myId,
		}
		_, err := b.cs.CitizenActions.Vote(ctx, &req)
		if err != nil {
			return stop, fmt.Errorf("failed to vote: %w", err)
		}
	} else if game.GetNight() != nil && isCriminal {
		// vote as a criminal
		target, found := b.findTargetForCriminalVote(game)
		if !found {
			return stop, fmt.Errorf("failed to find vote target")
		}
		fmt.Printf("Voting to kill %s\n", target)
		req := pb.CriminalVoteRequest{
			RoomId:        b.gameId,
			VictimId:      target,
			ParticipantId: b.myId,
		}
		_, err := b.cs.CriminalActions.Vote(ctx, &req)
		if err != nil {
			return stop, fmt.Errorf("failed to vote: %w", err)
		}
	} else if game.GetNight() != nil && myRole == pb.Role_POLICEMAN {
		// check somebody
		target := b.findTargetForCheck(game)
		fmt.Printf("Checking %s\n", target)
		req := pb.CheckRequest{
			RoomId:        b.gameId,
			SuspectedId:   target,
			ParticipantId: b.myId,
		}
		_, err := b.cs.PolicemanActions.Check(ctx, &req)
		if err != nil {
			return stop, fmt.Errorf("failed to check: %w", err)
		}
	}
	b.lastActivity = common.MakeComparisonKey(game)

	return madeAction, nil
}

func (b *Bot) getMyRole(ctx context.Context) (pb.Role, error) {
	req := pb.GetRequest{
		RoomId:        b.gameId,
		ParticipantId: b.myId,
	}
	res, err := b.cs.Games.Get(ctx, &req)
	if err != nil {
		return pb.Role_UNKNOWN, fmt.Errorf("failed to get game state")
	}
	for _, p := range res.Game.Participants {
		if p.Id == b.myId {
			return p.Role, nil
		}
	}
	return pb.Role_UNKNOWN, fmt.Errorf("failed to find my role")
}

func (b *Bot) trySendMessage(parentCtx context.Context, message string) {
	ctx, cancel := context.WithTimeout(parentCtx, time.Second*5)
	defer cancel()
	req := &pb.SendRequest{
		Topic: &pb.Topic{
			RoomId:   b.gameId,
			StreamId: "main",
		},
		Message: &pb.MessageData{
			ParticipantId: b.myId,
			Body:          message,
		},
	}
	_, err := b.cs.Chats.Send(ctx, req)
	if err != nil {
		fmt.Printf("Failed to send chat message: %v\n", err)
	}
}

func (b *Bot) run(ctx context.Context, chats pb.ChatServiceClient) error {
	fmt.Printf("Playing in automatic mode, game=%s, participant=%s\n", b.gameId, b.myId)
	b.trySendMessage(ctx, "Hello")
	printer := common.NewPrinter()
	err := b.waitStarted(ctx)
	if err != nil {
		return fmt.Errorf("failed to wait for game to start: %w", err)
	}
	b.trySendMessage(ctx, "GL&HF")
	myRole, err := b.getMyRole(ctx)
	if err != nil {
		return fmt.Errorf("failed to get my role")
	}

	fmt.Printf("My role is %s\n", myRole.String())

	for {
		req := pb.GetRequest{
			RoomId:        b.gameId,
			ParticipantId: b.myId,
		}
		res, err := b.cs.Games.Get(ctx, &req)
		if err != nil {
			return fmt.Errorf("failed to get game state")
		}
		printer.Print(res.Game)
		outcome, err := b.action(ctx, res.Game, myRole)
		if err != nil {
			return fmt.Errorf("failed to make move: %w", err)
		}
		switch outcome {
		case madeAction:
			// noop
		case needWait:
			fmt.Println("Waiting for game state to change")
			err = b.watcher.Wait(ctx, res.Game.Revision)
			if err != nil {
				return fmt.Errorf("watch failed: %w", err)
			}
		case stop:
			fmt.Println("Done")
			return nil
		default:
			panic("unknown outcome")
		}
	}
}

func RunAuto(ctx context.Context, cs client.ClientSet, gameId string, participantId string, watcher *common.Watcher) error {
	var b = Bot{
		cs:      cs,
		gameId:  gameId,
		watcher: watcher,
		myId:    participantId,
	}
	err := b.run(ctx, cs.Chats)
	return err
}
