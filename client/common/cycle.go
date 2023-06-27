package common

import "example.com/soa/pb"

func MakeComparisonKey(game *pb.Game) uint32 {
	if game.State == pb.Game_FINISHED {
		return (1 << 32) - 1
	}
	seq := game.CycleNumber
	isNight := game.GetNight() != nil
	seq *= 2
	if isNight {
		seq += 1
	}
	return seq
}
