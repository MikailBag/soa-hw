package main

import (
	"context"
	"encoding/base64"
	"flag"
	"fmt"
	"math/rand"
	"os"
	"os/signal"
	"strings"
	"syscall"
	"time"

	"example.com/soa/auto"
	"example.com/soa/chat"
	"example.com/soa/client"
	"example.com/soa/common"
	"example.com/soa/pb"
	"google.golang.org/grpc/codes"
	"google.golang.org/grpc/status"
)

func createGame(ctx context.Context, cs client.ClientSet) (string, error) {
	req := pb.CreateRequest{
		Settings: &pb.Settings{
			PlayerCount:    4,
			CriminalCount:  1,
			PolicemanCount: 1,
		},
		Title: "automatically generated game",
	}
	res, err := cs.Games.Create(ctx, &req)
	if err != nil {
		return "", fmt.Errorf("failed to create game: %w", err)
	}
	return res.GameId, nil
}

func findGame(ctx context.Context, cs client.ClientSet) (string, error) {
	req := pb.ListRequest{}

	res, err := cs.Games.List(ctx, &req)
	if err != nil {
		return "", fmt.Errorf("failed to list games: %w", err)
	}
	targets := make([]string, 0)
	for _, g := range res.Games {
		if g.State != pb.Game_NOT_STARTED {
			continue
		}
		targets = append(targets, g.Id)
	}
	if len(targets) == 0 {
		return "", fmt.Errorf("no games found")
	}
	if len(targets) > 1 {
		return "", fmt.Errorf("more than one game found: %d", len(targets))
	}
	return targets[0], nil
}

func generateName() string {
	variants := []string{
		"Jon Snow",
		"Peter Parker",
		"Eren Jaeger",
		"Harry Potter",
		"Frodo Baggins",
		"Arthas Menethil",
		"Clark Kent",
		"Sherlock Holmes",
		"Paul Atreides",
		"Thomas Anderson",
		"Anakin Skywalker",
		"Jim Raynor",
		"Professor Heimerdinger",
	}
	return variants[rand.Intn(len(variants))]
}

func isRetriableJoinError(e error) bool {
	return status.Code(e) == codes.AlreadyExists
}

func joinGame(ctx context.Context, cs client.ClientSet, gameId string) (string, error) {
	var err error
	for i := 0; i < 5; i++ {
		req := pb.JoinRequest{
			RoomId: gameId,
			Name:   generateName(),
		}
		var res *pb.JoinResponse
		res, err = cs.Games.Join(ctx, &req)
		if err != nil {
			if isRetriableJoinError(err) {
				time.Sleep(time.Second)
				continue
			}
			return "", fmt.Errorf("failed to join game: %w", err)
		}
		return res.ParticipantId, nil
	}
	return "", fmt.Errorf("all attempts to generate name failed, last error: %w", err)
}

func isGameFinished(game *pb.Game) common.PredicateResult {
	if game.State == pb.Game_FINISHED {
		return common.Yes
	}
	return common.Unknown
}

func makeContext() (context.Context, context.CancelFunc) {
	ctx, cancel := context.WithCancel(context.Background())
	ch := make(chan os.Signal, 1)
	signal.Notify(ch, syscall.SIGTERM, syscall.SIGINT)
	go func() {
		select {
		case <-ch:
			cancel()
		case <-ctx.Done():
		}
		select {
		case <-ch:
			fmt.Printf("Aborting\n")
			os.Exit(2)
		case <-ctx.Done():
		}
	}()
	return ctx, cancel
}

func parseBase64(encoded string) (string, error) {
	decoder := base64.NewDecoder(base64.StdEncoding, strings.NewReader(encoded))
	buf := make([]byte, 2*len(encoded))
	n, err := decoder.Read(buf)
	if err != nil {
		return "", fmt.Errorf("failed to decode base64: %w", err)
	}
	if n == len(buf) {
		panic("bad buffer capacity")
	}
	return string(buf), nil
}

func mainImpl() error {
	rand.Seed(time.Now().UnixMicro())
	address := flag.String("address", "127.0.0.1:8443", "game server RPC API address")
	autoMode := flag.Bool("auto", true, "play automatically")
	gameId := flag.String("game-id", "", "game id to use")
	joinExisting := flag.Bool("join-existing", false, "find and join existing game")

	flag.Parse()
	if !*autoMode {
		return fmt.Errorf("manual mode is unsupported")
	}
	if *joinExisting && len(*gameId) != 0 {
		return fmt.Errorf("join-existing and game-id are conflicting")
	}
	grpcCtx, cancelRootContext := makeContext()
	defer cancelRootContext()

	connCtx, connCtxCancel := context.WithTimeout(grpcCtx, 3*time.Second)
	defer connCtxCancel()
	cs, err := client.Connect(connCtx, *address)

	if err != nil {
		return fmt.Errorf("failed to connect to game server: %w", err)
	}
	defer cs.Close()

	if len(*gameId) == 0 {
		if *joinExisting {
			for i := 0; i < 5; i++ {
				*gameId, err = findGame(grpcCtx, cs)
				if err != nil {
					fmt.Println("Game not found, waiting")
					time.Sleep(time.Second)
					continue
				}
			}
			if err != nil {
				return fmt.Errorf("failed to find existing game: %w", err)
			}
		} else {
			*gameId, err = createGame(grpcCtx, cs)
			if err != nil {
				return err
			}
		}
		fmt.Printf("created game %s\n", *gameId)
	}
	username, err := joinGame(grpcCtx, cs, *gameId)
	if err != nil {
		return err
	}

	watcher := common.NewWatcher(grpcCtx, cs, *gameId)
	provider := common.NewGameProvider(cs, *gameId, username)

	go chat.Observe(grpcCtx, cs, *gameId, username, watcher, provider)

	if *autoMode {
		err = auto.RunAuto(grpcCtx, cs, *gameId, username, watcher)
		if err == nil {
			return nil
		}
		fmt.Printf("auto strategy failed: %v\n", err)
		_, err = common.WaitFor(grpcCtx, watcher, provider, isGameFinished)
		if err != nil {
			fmt.Printf("failed to wait for game to finish: %v\n", err)
		} else {
			fmt.Printf("[Game finished]\n")
		}
		return nil
	} else {
		panic("unreachable")
	}
}

func main() {
	err := mainImpl()
	if err != nil {
		fmt.Printf("error: %v\n", err)
		os.Exit(1)
	}
}
