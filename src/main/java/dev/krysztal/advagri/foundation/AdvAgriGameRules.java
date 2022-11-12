package dev.krysztal.advagri.foundation;

import lombok.extern.log4j.Log4j2;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.minecraft.text.Text;
import net.minecraft.world.GameRules;

@Log4j2
public class AdvAgriGameRules {

  public static final GameRules.Key<GameRules.BooleanRule> ALLOW_SEASONS_CHANGE;

  public static final GameRules.Key<GameRules.IntRule> LENGTH_OF_YEAR;

  private static final int SOLAR_TERM_COUNT = AdvAgriSolarTerm.values().length;

  public static void init() {}

  static {
    ALLOW_SEASONS_CHANGE =
      GameRuleRegistry.register(
        "allowSeasonsChange",
        GameRules.Category.UPDATES,
        GameRuleFactory.createBooleanRule(true)
      );

    LENGTH_OF_YEAR =
      GameRuleRegistry.register(
        "lengthOfYear",
        GameRules.Category.UPDATES,
        GameRuleFactory.createIntRule(
          SOLAR_TERM_COUNT,
          (
            (minecraftServer, intRule) -> {
              var ruleValue = intRule.get();
              var fixedValue = 0;

              if (ruleValue <= SOLAR_TERM_COUNT) {
                minecraftServer.sendMessage(
                  Text.translatable("adv_agri.game_rule.lengthOfYear.error3")
                );

                intRule.set(SOLAR_TERM_COUNT, minecraftServer);

                return;
              }

              if (ruleValue % SOLAR_TERM_COUNT != 0) {
                for (int i = ruleValue;; i++) if (i % SOLAR_TERM_COUNT == 0) {
                  fixedValue = i;
                  break;
                }

                minecraftServer.sendMessage(
                  Text.translatable(
                    "adv_agri.game_rule.lengthOfYear.error1",
                    ruleValue
                  )
                );

                minecraftServer.sendMessage(
                  Text.translatable(
                    "adv_agri.game_rule.lengthOfYear.error2",
                    fixedValue
                  )
                );

                intRule.set(fixedValue, minecraftServer);
              }
            }
          )
        )
      );
  }
}
