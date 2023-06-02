package com.example.demo.ctl;

import com.example.demo.api.game.GameOuterClass;
import org.springframework.util.comparator.Comparators;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

class VoteUtil {
    static List<String> getWinners(List<GameOuterClass.Vote> voteList) {
        if (voteList.isEmpty()) {
            throw new IllegalArgumentException("empty vote can not be finalized");
        }
        Map<String, Integer> votes = voteList
                .stream()
                .collect(Collectors.toMap(
                        GameOuterClass.Vote::getTargetId,
                        (v) -> 1,
                        Integer::sum
                ));
        int maxVotes = votes
                .values()
                .stream()
                .max(Comparators.comparable())
                .orElseThrow(() -> new IllegalStateException("trying to finalize vote when no citizens votes"));
        return votes
                .entrySet()
                .stream()
                .filter(e -> e.getValue().equals(maxVotes))
                .map(Map.Entry::getKey)
                .toList();
    }
}
