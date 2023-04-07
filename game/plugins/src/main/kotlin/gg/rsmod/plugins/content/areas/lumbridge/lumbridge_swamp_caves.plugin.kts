package gg.rsmod.plugins.content.areas.lumbridge

import gg.rsmod.plugins.content.mechanics.lightsource.LightSource

/**
 * @author Alycia <https://github.com/alycii>
 */

val SWAMP_CAVE_ROPE = AttributeKey<Boolean>(persistenceKey = "swamp_cave_rope")

val DARKNESS_TIMER = TimerKey()

val ATTACK_COUNT = AttributeKey<Int>()

// Candle Seller
on_npc_option(npc = Npcs.CANDLE_SELLER, option = "talk-to") {
    player.queue {
        chatNpc("Do you want a lit candle for 1000 gold?")
        when (options("Yes please.", "One thousand gold?!", "No thanks, I'd rather curse the darkness.")) {
            FIRST_OPTION -> {
                chatPlayer("Yes please.")
                if(player.inventory.remove(Items.COINS_995, 1000).hasSucceeded()) {
                    player.inventory.add(Items.LIT_CANDLE)
                    chatNpc("Here you go then.")
                    chatNpc(*"I should warn you, though, it can be dangerous to take a naked flame down there. You'd be better off making a lantern.".splitForDialogue())
                    when(options("What's so dangerous about a naked flame?", "How do you make lanterns?", "Thanks, bye.")) {
                        FIRST_OPTION -> {
                            chatPlayer("What's so dangerous about a naked flame?")
                            chatNpc("Heh heh... You'll find out.")
                        }
                        SECOND_OPTION -> {
                            chatPlayer("How do you make lanterns?")
                            chatNpc(*"Out of glass. The more advanced lanterns have a metal component as well.".splitForDialogue())
                            chatNpc(*"Firstly you can make a simple candle lantern out of glass. It's just like a candle, but the flame isn't exposed, so it's safer.".splitForDialogue())
                            chatNpc(*"Then you can make an oil lamp, which is brighter but has an exposed flame. BUt if you make an iron frame for it you can turn it into an oil lantern.".splitForDialogue())
                            chatNpc(*"Finally there's the bullseye lantern. You'll need to make a frame out of steel and add a glass lens.".splitForDialogue())
                            chatNpc(*"Once you've made your lamp or lantern, you'll need to make lamp oil for it. The chemist near Rimmington has a machine for that.".splitForDialogue())
                            chatNpc(*"For any light source, you'll need a tinderbox to light it. Keep your tinderbox handy in case it goes out!".splitForDialogue())
                            chatNpc(*"But if all that's too complicated, you can buy a candle right here for 1000 gold!".splitForDialogue())
                            chatPlayer("Thanks, bye.")
                        }
                        THIRD_OPTION -> {
                            chatPlayer("Thanks, bye.")
                        }
                    }
                } else {
                    chatPlayer("But I don't have that kind of money on me.")
                }
            }
            SECOND_OPTION -> {
                chatPlayer("One thousand gold?!")
                chatNpc(*"Look, you're not going to be able to survive down that hole without a light source.".splitForDialogue())
                chatNpc(*"So you could go off to the candle shop to buy one more cheaply. You could even make your own lantern, which is a lot better.".splitForDialogue())
                chatNpc(*"But I bet you want to find out what's down there right now, don't you? And you can pay me 1000 gold for the privilege!".splitForDialogue())
            }
            THIRD_OPTION -> {
                chatPlayer("No thanks, I'd rather curse the darkness.")
            }
        }
    }
}

on_item_on_obj(obj = Objs.DARK_HOLE_UNDER_TREE, item = Items.ROPE) {
    if(!player.attr.has(SWAMP_CAVE_ROPE)) {
        player.queue {
            player.inventory.remove(Items.ROPE)
            player.attr[SWAMP_CAVE_ROPE] = true
            itemMessageBox("You tie the rope to the top of the entrance and throw it down.", item = Items.ROPE)
        }
    } else {
        player.message("You have already attached a rope here.")
    }
}

on_obj_option(obj = Objs.DARK_HOLE_UNDER_TREE, option = "climb-down") {
    if(!player.attr.has(SWAMP_CAVE_ROPE)) {
        player.queue {
            messageBox("There is a sheer drop below the hole. You will need a rope.")
        }
        return@on_obj_option
    }
    player.handleBasicLadder(false, x = 3168, z = 9572)
}

on_obj_option(obj = Objs.CLIMBING_ROPE_5946, option = "climb") {
    player.handleBasicLadder(true, x = 3168, z = 3171)
}

on_enter_region(regionId = 12693) {
    val lightSource = LightSource.getActiveLightSource(player)
    if(lightSource == null) {
        player.openInterface(dest = InterfaceDestination.MAIN_SCREEN_OVERLAY, interfaceId = 96)
        player.timers[DARKNESS_TIMER] = 10
    } else {
        player.openInterface(dest = InterfaceDestination.MAIN_SCREEN_OVERLAY, interfaceId = lightSource.interfaceId)
    }
}

on_exit_region(regionId = 12693) {
    player.timers.remove(DARKNESS_TIMER)
    player.closeMainInterface()
}

on_timer(DARKNESS_TIMER) {
    if (player.attr[ATTACK_COUNT] == null) {
        player.attr[ATTACK_COUNT] = 0
    }
    when(player.attr[ATTACK_COUNT]) {
        0 -> {}
        1 -> player.message("You hear tiny insects skittering over the ground...")
        2 ->  player.message("Tiny biting insects swarm all over you!")
        else -> player.hit(damage = world.random(10..100), type = HitType.REGULAR_HIT)
    }
    player.attr[ATTACK_COUNT] = (player.attr[ATTACK_COUNT] ?: 0) + 1
    player.timers[DARKNESS_TIMER] = 10
}