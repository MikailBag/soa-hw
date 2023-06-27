package client

import (
	"context"
	"fmt"
	"time"

	"example.com/soa/pb"
	"google.golang.org/grpc"
	"google.golang.org/grpc/credentials/insecure"
)

type ClientSet struct {
	channel          *grpc.ClientConn
	Chats            pb.ChatServiceClient
	Games            pb.GameServiceClient
	CitizenActions   pb.CitizenServiceClient
	CriminalActions  pb.CriminalServiceClient
	PolicemanActions pb.PolicemanServiceClient
}

func Connect(ctx context.Context, address string) (ClientSet, error) {
	var err error
	var ch *grpc.ClientConn
	for ctx.Err() == nil {
		ch, err = grpc.DialContext(ctx, address, grpc.WithTransportCredentials(insecure.NewCredentials()), grpc.WithBlock())
		if err != nil {
			time.Sleep(250 * time.Millisecond)
			continue
		}
		cs := ClientSet{
			channel:          ch,
			Chats:            pb.NewChatServiceClient(ch),
			Games:            pb.NewGameServiceClient(ch),
			CitizenActions:   pb.NewCitizenServiceClient(ch),
			CriminalActions:  pb.NewCriminalServiceClient(ch),
			PolicemanActions: pb.NewPolicemanServiceClient(ch),
		}

		return cs, nil
	}
	return ClientSet{}, fmt.Errorf("failed to create gRPC channel within timeout, last error was: %w", err)
}

func (cs ClientSet) Close() {
	err := cs.channel.Close()
	if err != nil {
		fmt.Printf("failed to close client: %v\n", err)
	}
}
