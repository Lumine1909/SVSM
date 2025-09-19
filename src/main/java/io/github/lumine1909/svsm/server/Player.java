package io.github.lumine1909.svsm.server;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.timeout.ReadTimeoutHandler;
import net.minecraft.Util;
import net.minecraft.network.VarInt;
import net.minecraft.network.protocol.common.CommonPacketTypes;
import net.minecraft.network.protocol.game.GameProtocols;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.craftbukkit.entity.CraftPlayer;

import java.util.UUID;

public class Player {

    private static final long KEEP_ALIVE_PERIOD = 5000;
    private static final int KEEP_ALIVE;

    static {
        int[] id = {0};
        GameProtocols.CLIENTBOUND_TEMPLATE.details().listPackets((packetType, i) -> {
            if (packetType.equals(CommonPacketTypes.CLIENTBOUND_KEEP_ALIVE)) {
                id[0] = i;
            }
        });
        KEEP_ALIVE = id[0];
    }

    private PlayerInfo info;
    private long prevKeepAlive = Util.getMillis();

    private Player() {
    }

    public static Player createFromBukkit(org.bukkit.entity.Player bukkitPlayer) {
        ServerPlayer sp = ((CraftPlayer) bukkitPlayer).getHandle();
        Player player = new Player();
        Channel channel = sp.connection.connection.channel;
        if (channel.pipeline().get("svsm_handler") != null) {
            channel.pipeline().remove("svsm_handler");
        }
        channel.pipeline().replace("timeout", "timeout", new ReadTimeoutHandler(Integer.MAX_VALUE));
        channel.pipeline().addBefore("encoder", "svsm_handler", new ChannelDuplexHandler() {
            @Override
            public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                if (msg instanceof ByteBuf buf) {
                    if (VarInt.read(buf) == KEEP_ALIVE) {
                        player.prevKeepAlive = buf.readLong();
                    }
                    buf.readerIndex(0);
                }
                super.write(ctx, msg, promise);
            }
        });
        player.info = new PlayerInfo(sp.getScoreboardName(), sp.getUUID(), channel, channel.pipeline().context("svsm_handler"));
        VirtualServer.SERVER.playerConnect(player);
        channel.closeFuture().addListener(f -> player.handleDisconnect());
        return player;
    }

    public PlayerInfo info() {
        return info;
    }

    public void keepAlive() {
        long curr = Util.getMillis();
        if (curr - prevKeepAlive > KEEP_ALIVE_PERIOD) {
            sendKeepAlivePacket();
            prevKeepAlive = Util.getMillis();
        }
    }

    private void handleDisconnect() {
        VirtualServer.SERVER.playerDisconnect(this);
    }

    private void sendKeepAlivePacket() {
        ByteBuf buf = Unpooled.buffer();
        VarInt.write(buf, KEEP_ALIVE);
        buf.writeLong(Util.getMillis());
        info.ctx.writeAndFlush(buf);
    }

    public record PlayerInfo(String name, UUID uuid, Channel channel, ChannelHandlerContext ctx) {

    }
}