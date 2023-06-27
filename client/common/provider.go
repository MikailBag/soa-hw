package common

import (
	"context"
	"fmt"
	"sync"

	"example.com/soa/client"
	"example.com/soa/pb"
)

type GameProvider struct {
	games         pb.GameServiceClient
	gameId        string
	participantId string
	mu            sync.Mutex
	cur           *pb.Game
}

func (gp *GameProvider) refresh(ctx context.Context) error {
	req := pb.GetRequest{
		RoomId: gp.gameId,
		ParticipantId: gp.participantId,
	}

	res, err := gp.games.Get(ctx, &req)
	if err != nil {
		return fmt.Errorf("failed to get game: %w", err)
	}
	gp.mu.Lock()
	defer gp.mu.Unlock()
	if gp.cur == nil || gp.cur.Revision < res.Game.Revision {
		gp.cur = res.Game
	}
	return nil
}

func (gp *GameProvider) tryGetAfter(after uint32) *pb.Game {
	gp.mu.Lock()
	defer gp.mu.Unlock()
	if gp.cur != nil && gp.cur.Revision > after {
		return gp.cur
	}
	return nil
}

func (gp *GameProvider) GetAfter(ctx context.Context, after uint32) (*pb.Game, error) {
	game := gp.tryGetAfter(after)
	if game != nil {
		return game, nil
	}
	err := gp.refresh(ctx)
	if err != nil {
		return nil, fmt.Errorf("failed to refresh game: %w", err)
	}
	game = gp.tryGetAfter(after)
	if game == nil {
		panic("failed to satisfy causal requirement even after refresh, call to watcher is missing")
	}
	return game, nil
}

func NewGameProvider(cs client.ClientSet, gameId string, participantId string) *GameProvider {
	return &GameProvider{
		games: cs.Games,
		gameId: gameId,
		participantId: participantId,
	}
}
