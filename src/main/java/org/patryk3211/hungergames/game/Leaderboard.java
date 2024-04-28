package org.patryk3211.hungergames.game;

import io.papermc.paper.scoreboard.numbers.NumberFormat;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static net.kyori.adventure.text.format.NamedTextColor.*;

public class Leaderboard {
    private final ScoreboardManager scoreboardManager;
    private final Map<Integer, ILeaderboardProvider> providers = new TreeMap<>();

    public Leaderboard(Server server) {
        this.scoreboardManager = server.getScoreboardManager();
    }

    public void addProvider(int location, ILeaderboardProvider provider) {
        providers.put(location, provider);
    }

    public void removeProvider(int location) {
        providers.remove(location);
    }

    public void updateScoreboard(TrackedPlayerData data) {
        Objective objective = data.scoreboard.getObjective("all");
        if(objective == null)
            throw new IllegalStateException("Custom scoreboard has to have the correct objectives");

        List<Component> lines = new LinkedList<>();
        lines.add(Component.join(
                JoinConfiguration.noSeparators(),
                Component.text(" Śmierci: ", GRAY),
                Component.text(data.getDeaths(), RED)
        ));
        lines.add(Component.join(
                JoinConfiguration.noSeparators(),
                Component.text(" Zabójstwa: ", GRAY),
                Component.text(data.getKills(), RED)
        ));

        providers.forEach((_location, provider) -> {
            Component[] providedLines = provider.lines();
            if(providedLines == null)
                return;
            lines.addAll(List.of(providedLines));
        });

        int i = 0;
        while(i < lines.size()) {
            Score lineScore = objective.getScore("line" + i);
            lineScore.customName(lines.get(i));
            lineScore.setScore(lines.size() - i);
            lineScore.numberFormat(NumberFormat.blank());
            ++i;
        }

        while(objective.getScore("line" + i).isScoreSet()) {
            objective.getScore("line" + i).resetScore();
            ++i;
        }
    }

    public void showTo(Player player, TrackedPlayerData tracker) {
        if(tracker.scoreboard == null) {
            final Scoreboard scoreboard = scoreboardManager.getNewScoreboard();
            Objective objective = scoreboard.registerNewObjective("all", Criteria.DUMMY,
                    Component.join(JoinConfiguration.noSeparators(),
                            Component.text("-=-=- ", DARK_GRAY),
                            Component.text("Statystyki", GOLD),
                            Component.text(" -=-=-", DARK_GRAY)
                    )
            );
            objective.setDisplaySlot(DisplaySlot.SIDEBAR);

            player.setScoreboard(scoreboard);
            tracker.scoreboard = scoreboard;
        } else {
            player.setScoreboard(tracker.scoreboard);
        }
        updateScoreboard(tracker);
    }
}
