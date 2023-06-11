package com.example.demo.report;

import com.example.demo.api.game.GameOuterClass;
import com.example.demo.model.User;
import com.example.demo.repo.GameRepository;
import com.example.demo.repo.UserRepository;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.PdfWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
class ReportGenerator {
    private final GameRepository games;
    private final UserRepository users;

    @Autowired
    ReportGenerator(
            GameRepository games,
            UserRepository users
    ) {
        this.games = games;
        this.users = users;
    }

    private record GameStats(
            int winCount,
            int defeatCount,
            Duration duration
    ) {
        static GameStats empty() {
            return new GameStats(0, 0, Duration.ZERO);
        }
        static GameStats of(GameOuterClass.Game game, String login) {
            GameOuterClass.Participant participant = game
                    .getParticipantsList()
                    .stream()
                    .filter(p -> p.getId().equals(login))
                    .findFirst()
                    .orElseThrow(IllegalStateException::new);
            Duration duration = Duration.ofNanos(1);
            boolean isCriminal = Set.of(
                    GameOuterClass.Role.CRIMINAL, GameOuterClass.Role.CRIMINAL_BOSS
            ).contains(participant.getRole());
            boolean criminalsWon = switch (game.getOutcome()) {
                case CITIZENS_WON -> false;
                case CRIMINALS_WON -> true;
                default -> throw new IllegalStateException();
            };
            if (isCriminal == criminalsWon) {
                return new GameStats(1, 0, duration);
            } else {
                return new GameStats(0, 1, duration);
            }
        }

        static GameStats merge(GameStats lhs, GameStats rhs) {
            return new GameStats(
                    lhs.winCount() + rhs.winCount(),
                    lhs.defeatCount() + rhs.defeatCount(),
                    lhs.duration().plus(rhs.duration())
            );
        }
    }

    private void emitDocument(String login, Document document, List<String> messages) throws DocumentException, IOException {
        messages.add(("Report for: " + login));
        User user = users.find(login);
        if (user == null) {
            messages.add(("User does not exist"));
            return;
        }
        messages.add("User info:");
        messages.add(("\tEmail: " + user.email() ));
        messages.add(("\tGender: " + user.gender() ));
        if (user.picture().length > 0) {
            document.add(Image.getInstance(user.picture()));
        }
        GameStats stats = games
                .list()
                .stream()
                .filter(g -> g.getState().equals(GameOuterClass.Game.State.FINISHED))
                .filter(g -> g.getParticipantsList().stream().anyMatch(p -> p.getId().equals(login)))
                .map(g -> GameStats.of(g, login))
                .reduce(GameStats.empty(), GameStats::merge);
        messages.add(("Won in " + stats.winCount() + " games"));
        messages.add(("Defeated in " + stats.defeatCount() + " games"));
        messages.add(("Total time spent is " + stats.duration().toString()));
    }

    public byte[] generate(String user) throws DocumentException, IOException {
        Document doc = new Document();
        var stream = new ByteArrayOutputStream();
        PdfWriter.getInstance(doc, stream);
        doc.open();
        var msgs = new ArrayList<String>();
        emitDocument(user, doc, msgs);
        doc.add(new Chunk(String.join("\n", msgs)));
        doc.close();
        return stream.toByteArray();
    }
}
