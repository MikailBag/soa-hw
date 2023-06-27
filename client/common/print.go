package common

import (
	"fmt"

	"example.com/soa/pb"
)

func PrintGame(game *pb.Game) error {
	fmt.Printf("Game: %s [rev %d]\n", game.Title, game.Revision)
	switch game.State {
	case pb.Game_NOT_STARTED:
		fmt.Printf("Game not started\n")
	case pb.Game_FINISHED:
		fmt.Printf("Game finished, outcome %v\n", game.Outcome)
	case pb.Game_IN_PROGRESS:
		var phase string
		if game.GetNight() != nil {
			phase = "night"
		} else if game.GetDay() != nil {
			phase = "day"
		} else {
			return fmt.Errorf("unknown game phase: %v", game)
		}
		fmt.Printf("Game is in progress, game time is %s %d\n", phase, game.CycleNumber)
	}

	for _, p := range game.Participants {
		fmt.Printf("[%s] %s (%v): %v\n", p.Id, p.Name, p.Role, p.State)
	}
	return nil
}

type Printer struct {
	curKey uint32
}

func NewPrinter() *Printer {
	return &Printer{}
}

func (p *Printer) Print(game *pb.Game) {
	newKey := MakeComparisonKey(game)
	if newKey < p.curKey {
		panic("time went backwards")
	}
	if newKey == p.curKey {
		return
	}
	p.curKey = newKey
	err := PrintGame(game)
	if err != nil {
		fmt.Printf("failed to print game: %v\n", err)
	}
}
