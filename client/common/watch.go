package common

import (
	"context"
	"fmt"
	"sync"
	"time"

	"example.com/soa/client"
	"example.com/soa/pb"
)

type empty struct{}

type node struct {
	observed uint32
	wakeup   chan empty
}

type Watcher struct {
	games       pb.GameServiceClient
	gameId      string
	errorStreak uint32
	mu          *sync.Mutex
	current     uint32
	closed      bool
	waiters     []*node
}

func NewWatcher(ctx context.Context, cs client.ClientSet, gameId string) *Watcher {
	w := Watcher{
		games:   cs.Games,
		mu:      &sync.Mutex{},
		waiters: make([]*node, 0),
		gameId:  gameId,
	}

	go w.run(ctx)

	return &w
}

func (w *Watcher) poll(ctx context.Context) error {
	var cur uint32
	{
		w.mu.Lock()
		cur = w.current
		w.mu.Unlock()
	}
	req := pb.PollRequest{
		GameId:           w.gameId,
		ObservedRevision: cur,
	}
	res, err := w.games.Poll(ctx, &req)
	if err != nil {
		return fmt.Errorf("failed to invoke GameService.Poll: %w", err)
	}
	if res.GetTimeout() {
		// fmt.Println("Game state not changed yet, restarting poll")
		return nil
	}
	if res.GetRevision() <= cur {
		return fmt.Errorf("invalid server behavior: old revision %d, new %d", cur, res.GetRevision())
	}
	w.notify(res.GetRevision())
	return nil
}

func (w *Watcher) run(ctx context.Context) {
	for ctx.Err() == nil {
		err := w.poll(ctx)
		if err != nil {
			w.errorStreak += 1
			fmt.Printf("Watch error (%d): %v\n", w.errorStreak, err)
			if w.errorStreak == 10 {
				fmt.Println("Error limit exceeded, aborting watch")
				break
			}
			time.Sleep(time.Second)
		} else {
			w.errorStreak = 0
		}
	}
	w.mu.Lock()
	defer w.mu.Unlock()
	w.closed = true
	for _, n := range w.waiters {
		n.wakeup <- empty{}
	}
}

func (w *Watcher) enqueue(ch chan empty, after uint32) {
	n := &node{
		wakeup:   ch,
		observed: after,
	}
	w.mu.Lock()
	defer w.mu.Unlock()
	if w.current > after {
		ch <- empty{}
		return
	}
	if w.closed {
		ch <- empty{}
		return
	}
	w.waiters = append(w.waiters, n)
}

func (w *Watcher) notify(rev uint32) {
	w.mu.Lock()
	defer w.mu.Unlock()
	if rev <= w.current {
		panic("revision was not monotonic")
	}
	newWaiters := make([]*node, 0)
	for _, n := range w.waiters {
		if rev > n.observed {
			n.wakeup <- empty{}
		} else {
			newWaiters = append(newWaiters, n)
		}
	}
	w.waiters = newWaiters
	// fmt.Printf("Game revision is now %d\n", rev)
	w.current = rev
}

func (w *Watcher) Wait(ctx context.Context, after uint32) error {
	if after == 0 {
		return fmt.Errorf("invalid revision")
	}
	ch := make(chan empty, 1)

	w.enqueue(ch, after)
	select {
	case <-ctx.Done():
		return fmt.Errorf("wait was interrupted: %w", ctx.Err())
	case <-ch:
	}
	w.mu.Lock()
	defer w.mu.Unlock()
	if w.current > after {
		return nil
	}
	if w.closed {
		return fmt.Errorf("watcher was stopped")
	}
	panic("unexpected state")
}
