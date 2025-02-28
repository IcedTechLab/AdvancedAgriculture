/*
 * MIT License
 *
 * Copyright (c) 2024 KrysztalHuang
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package dev.krysztal.are.common.component

import dev.krysztal.are.common.WorldComponents.SEASON_COMPONENT
import dev.krysztal.are.common.component.SeasonComponentImpl.log
import dev.krysztal.are.foundation.Season
import net.minecraft.nbt.NbtCompound
import net.minecraft.registry.RegistryWrapper.WrapperLookup
import net.minecraft.world.World
import org.ladysnake.cca.api.v3.component.ComponentV3
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent
import org.slf4j.LoggerFactory

trait SeasonComponent extends ComponentV3 {
    var season: Season = Season.Spring
    private[component] var shouldChangeSeason: Boolean = false
}

/** [[SeasonComponent]] implementation.
  *
  * This class implemented [[SeasonComponent]], the season changed by the moon
  * phase.
  *
  * @author
  *   Krysztal Huang <krysztal.huang@outlook.com>
  * @param world
  */
class SeasonComponentImpl(val world: World)
    extends SeasonComponent,
      AutoSyncedComponent,
      ServerTickingComponent {

    override def serverTick(): Unit = {
        tickSeason()
    }

    override def writeToNbt(
        tag: NbtCompound,
        registryLookup: WrapperLookup
    ): Unit = {
        tag.putString("season", season.toString)
        tag.putBoolean("shouldChangeSeason", shouldChangeSeason)
    }

    override def readFromNbt(
        tag: NbtCompound,
        registryLookup: WrapperLookup
    ): Unit = {
        season = Season.valueOf(tag.getString("season"))
        shouldChangeSeason = tag.getBoolean("shouldChangeSeason")
    }

    def tickSeason(): Unit = {
        val moonPhase = this.world.getMoonPhase()

        if (moonPhase != 0 || moonPhase != 7) {
            return;
        }

        if (moonPhase == 7 && this.shouldChangeSeason == false) {
            shouldChangeSeason = true
            return
        }

        if (moonPhase == 0 && this.shouldChangeSeason) {
            val toSeason = Season.fromOrdinal((this.season.ordinal + 1) % 4)
            log.debug(
              s"The season of world $world have been changed from $season to $toSeason"
            )
            this.season = toSeason
            this.shouldChangeSeason = false
            SEASON_COMPONENT.sync(this.world)
        }

    }
}

object SeasonComponentImpl {
    private lazy val log = LoggerFactory.getLogger("SeasonComponent")

    def apply(world: World) = new SeasonComponentImpl(world)
}
