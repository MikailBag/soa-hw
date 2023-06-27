package chat

import (
	"context"
	"fmt"
	"sync"

	"example.com/soa/client"
	"example.com/soa/common"
	"example.com/soa/pb"
)

type streamObserver struct {
	chat   pb.ChatServiceClient
	gameId string
	myId   string
	stream string
}

func (s *streamObserver) run(ctx context.Context) error {
	if s == nil {
		panic("bug")
	}
	req := pb.WatchRequest{
		Topic: &pb.Topic{
			RoomId:   s.gameId,
			StreamId: s.stream,
		},
		ParticipantId: s.myId,
	}
	watcher, err := s.chat.Watch(ctx, &req)
	if err != nil {
		return fmt.Errorf("failed to start watch: %w", err)
	}
	fmt.Printf("Now watching stream %v\n", s.stream)
	for {
		event, err := watcher.Recv()
		if err != nil {
			return fmt.Errorf("failed to receive next event: %w", err)
		}
		fmt.Printf("<%v@%v> %v\n", event.Message.ParticipantId, s.stream, event.Message.Body)
		if event.Message.Eof {
			break
		}
	}
	return nil
}

type observerState struct {
	gameId string
	myId   string
	chats  pb.ChatServiceClient
	ctx    context.Context
	done   chan error
	mu     sync.Mutex
	closed bool
}

func (os *observerState) addStream(stream string) {
	os.mu.Lock()
	defer os.mu.Unlock()
	if os.closed {
		return
	}
	if os.chats == nil {
		panic("bug")
	}
	obs := &streamObserver{
		chat:   os.chats,
		gameId: os.gameId,
		myId:   os.myId,
		stream: stream,
	}
	go func() {
		os.done <- obs.run(os.ctx)
	}()
}

func (os *observerState) abandon() {
	os.mu.Lock()
	defer os.mu.Unlock()

	os.done <- nil
}

func (os *observerState) join(count int) error {
	for i := 0; i < count; i++ {
		err := <-os.done
		if err != nil {
			return fmt.Errorf("stream observer failed: %w", err)
		}
	}
	return nil
}

func mainStreamPredicate(game *pb.Game) common.PredicateResult {
	if game.GetState() == pb.Game_FINISHED {
		return common.No
	}
	return common.Yes
}

func makeDeadStreamPredicate(myId string) common.GamePredicate {
	return func(game *pb.Game) common.PredicateResult {
		if game.GetState() == pb.Game_FINISHED {
			return common.No
		}
		if game.GetState() == pb.Game_NOT_STARTED {
			return common.Unknown
		}
		for _, p := range game.Participants {
			if p.Id != myId {
				continue
			}
			if p.State == pb.Participant_KILLED_AT_DAY || p.State == pb.Participant_KILLED_AT_NIGHT {
				return common.Yes
			}
		}
		return common.Unknown
	}
}

func makeCriminalStreamPredicate(myId string) common.GamePredicate {
	return func(game *pb.Game) common.PredicateResult {
		if game.GetState() == pb.Game_FINISHED {
			return common.No
		}
		if game.GetState() == pb.Game_NOT_STARTED {
			return common.Unknown
		}
		for _, p := range game.Participants {
			if p.Id != myId {
				continue
			}
			if p.Role == pb.Role_CRIMINAL || p.Role == pb.Role_CRIMINAL_BOSS {
				return common.Yes
			} else {
				return common.No
			}
		}
		return common.Unknown
	}
}

func addStreamIf(ctx context.Context, os *observerState, w *common.Watcher, gp *common.GameProvider, streamName string, pred common.GamePredicate) {
	holds, err := common.WaitFor(ctx, w, gp, pred)
	if err != nil {
		os.abandon()
		fmt.Printf("will not subscribe to stream %v: failed to wait for predicate: %v\n", streamName, err)
		return
	}
	if !holds {
		fmt.Printf("will not subscribe to stream %v: not eligible\n", streamName)
		os.abandon()
		return
	}
	os.addStream(streamName)
}

func Observe(parentCtx context.Context, cs client.ClientSet, gameId string, myId string, watcher *common.Watcher, provider *common.GameProvider) error {
	ctx, cancel := context.WithCancel(parentCtx)
	defer cancel()
	doneCh := make(chan error)
	if cs.Chats == nil {
		panic("Chats is nil")
	}
	if watcher == nil {
		panic("watcher is nil")
	}
	if provider == nil {
		panic("provider is nil")
	}
	os := &observerState{
		ctx:    ctx,
		chats:  cs.Chats,
		gameId: gameId,
		myId:   myId,
		done:   doneCh,
	}
	addStreamIf(ctx, os, watcher, provider, "main", mainStreamPredicate)
	addStreamIf(ctx, os, watcher, provider, "criminal", makeCriminalStreamPredicate(myId))
	addStreamIf(ctx, os, watcher, provider, "dead", makeDeadStreamPredicate(myId))
	err := os.join(3 /*main + dead + criminal*/)
	if err != nil {
		cancel()
		return fmt.Errorf("chat observer failed: %w", err)
	}
	return nil
}
