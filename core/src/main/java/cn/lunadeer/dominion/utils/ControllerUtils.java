package cn.lunadeer.dominion.utils;

import cn.lunadeer.dominion.CacheImpl;
import cn.lunadeer.dominion.Dominion;
import cn.lunadeer.dominion.controllers.AbstractOperator;
import cn.lunadeer.dominion.dtos.DominionDTO;
import cn.lunadeer.dominion.dtos.GroupDTO;
import cn.lunadeer.dominion.dtos.MemberDTO;
import cn.lunadeer.dominion.managers.Translation;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

public class ControllerUtils {

    public static boolean notOwner(AbstractOperator player, DominionDTO dominion) {
        if (player.isOp() && Dominion.config.getLimitOpBypass()) return false;
        return !dominion.getOwner().equals(player.getUniqueId());
    }

    public static boolean noAuthToChangeFlags(AbstractOperator player, DominionDTO dominion) {
        if (player.isOp() && Dominion.config.getLimitOpBypass()) return false;
        if (!dominion.getOwner().equals(player.getUniqueId())) {
            MemberDTO privileges = MemberDTO.select(player.getUniqueId(), dominion.getId());
            if (privileges == null || !privileges.getAdmin()) {
                player.setResponse(new AbstractOperator.Result(AbstractOperator.Result.FAILURE, Translation.Messages_NotDominionOwnerOrAdmin, dominion.getName()));
                return true;
            }
        }
        return false;
    }

    /**
     * 获取玩家当前所在的领地
     * 如果玩家不在一个领地内或者在子领地内，会提示玩家手动指定要操作的领地名称
     *
     * @param player 玩家
     * @return 当前所在的领地
     */
    public static DominionDTO getPlayerCurrentDominion(AbstractOperator player) {
        Location location = player.getLocation();
        if (location == null) {
            player.setResponse(new AbstractOperator.Result(AbstractOperator.Result.FAILURE, Translation.Messages_CannotGetDominionAuto));
            return null;
        }
        DominionDTO dominion = CacheImpl.instance.getDominionByLoc(location);
        if (dominion == null) {
            return null;
        }
        if (dominion.getParentDomId() == -1) {
            return dominion;
        } else {
            player.setResponse(new AbstractOperator.Result(AbstractOperator.Result.FAILURE, Translation.Messages_InSubDominion));
            return null;
        }
    }

    /**
     * 检查一个成员是否是管理员
     * 此方法会同时尝试搜索玩家所在的权限组是否是管理员
     *
     * @param member 成员权限
     * @return 是否是管理员
     */
    public static boolean isAdmin(@NotNull MemberDTO member) {
        GroupDTO group = GroupDTO.select(member.getGroupId());
        if (group == null) {
            return member.getAdmin();
        } else {
            return group.getAdmin();
        }
    }

}
