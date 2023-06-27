package common

import (
	"context"
	"fmt"

	"example.com/soa/pb"
)

type PredicateResult int

const (
	Yes PredicateResult = iota
	No
	Unknown
)

type GamePredicate func(*pb.Game) PredicateResult

func WaitFor(ctx context.Context, watcher *Watcher, gameProvider *GameProvider, predicate GamePredicate) (bool, error) {
	var curRev uint32
	for {
		game, err := gameProvider.GetAfter(ctx, curRev)
		if err != nil {
			return false, fmt.Errorf("failed to get new game: %w", err)
		}
		res := predicate(game)
		switch res {
		case Yes:
			return true, nil
		case No:
			return false, nil
		}
		curRev = game.Revision
		err = watcher.Wait(ctx, curRev)
		if err != nil {
			return false, fmt.Errorf("failed to wait for game change: %w", err)
		}
	}
}
