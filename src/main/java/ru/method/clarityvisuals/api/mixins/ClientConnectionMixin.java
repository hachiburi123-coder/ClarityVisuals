package ru.method.clarityvisuals.api.mixins;

import io.netty.channel.ChannelHandlerContext;
import net.minecraft.class_2535;
import net.minecraft.class_2596;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.method.clarityvisuals.ClarityVisuals;
import ru.method.clarityvisuals.api.event.PacketEvent;
import ru.method.clarityvisuals.network.PacketSuppression;

@Mixin(value={class_2535.class})
public abstract class ClientConnectionMixin {
    @Inject(method={"channelRead0(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/packet/Packet;)V"}, at={@At(value="HEAD")}, cancellable=true)
    public void channelRead0(ChannelHandlerContext channelHandlerContext, class_2596<?> class_25962, CallbackInfo callbackInfo) {
        PacketEvent.Receive receive = new PacketEvent.Receive(class_25962);
        ClarityVisuals.getInstance().getEventHandler().post((Object)receive);
        if (receive.isCancelled()) {
            callbackInfo.cancel();
        }
    }

    @Inject(method={"send(Lnet/minecraft/network/packet/Packet;)V"}, at={@At(value="HEAD")}, cancellable=true)
    public void send(class_2596<?> class_25962, CallbackInfo callbackInfo) {
        if (PacketSuppression.c(class_25962)) {
            return;
        }
        PacketEvent.Send send = new PacketEvent.Send(class_25962);
        ClarityVisuals.getInstance().getEventHandler().post((Object)send);
        if (send.isCancelled()) {
            callbackInfo.cancel();
        }
    }
}

