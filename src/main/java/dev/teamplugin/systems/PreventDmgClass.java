package dev.teamplugin.systems;

import com.hypixel.hytale.component.*;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.knockback.KnockbackComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageEventSystem;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageModule;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import dev.teamplugin.TeamPlugin;
import dev.teamplugin.data.PlayerTeam;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * System that filters player vs player damage based on PvP toggle state.
 * Damage is cancelled if either the attacker or the target has PvP disabled.
 */
public class PreventDmgClass extends DamageEventSystem {
    private final TeamPlugin plugin;

    public PreventDmgClass(TeamPlugin plugin){
        this.plugin = plugin;
    }



    @Nullable
    @Override
    public SystemGroup<EntityStore> getGroup() {
        return DamageModule.get().getFilterDamageGroup();
    }

    @Nonnull
    @Override
    public Query<EntityStore> getQuery() {
        return UUIDComponent.getComponentType();
    }

    @Override
    public void handle(
            int index,
            @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
            @Nonnull Store<EntityStore> store,
            @Nonnull CommandBuffer<EntityStore> commandBuffer,
            @Nonnull Damage damage
    ) {
        if (damage.isCancelled()) {
            return;
        }
        // Check the damage source and make sure its valid
        Damage.Source source = damage.getSource();
        if (!(source instanceof Damage.EntitySource entitySource)) {
            return;
        }
        // Grab the attacker, if we cant: return
        Ref<EntityStore> attackerRef = entitySource.getRef();
        if (!attackerRef.isValid()) {
            return;
        }

        UUIDComponent attackerComponent = store.getComponent(attackerRef, UUIDComponent.getComponentType());
        assert attackerComponent != null;

        Ref<EntityStore> targetRef = archetypeChunk.getReferenceTo(index);
        UUIDComponent targetComponent = store.getComponent(targetRef, UUIDComponent.getComponentType());
        assert targetComponent != null;

        PlayerTeam attackerTeam = plugin.getStorage().getTeamByPlayer(attackerComponent.getUuid());
        PlayerTeam targetTeam = plugin.getStorage().getTeamByPlayer(targetComponent.getUuid());

        // If either player is not on a team, return
        if (attackerTeam == null || targetTeam == null){
            return;
        }
        // If they are not on the same team, return
        if (!attackerTeam.getTeamName().equals(targetTeam.getTeamName())){
            return;
        }


        // If pvp is disabled we cancel our pvp and delete knockback
        if (!targetTeam.getFriendlyPVP()) {
            damage.setCancelled(true);
            commandBuffer.tryRemoveComponent(targetRef, KnockbackComponent.getComponentType());
        }
    }
}