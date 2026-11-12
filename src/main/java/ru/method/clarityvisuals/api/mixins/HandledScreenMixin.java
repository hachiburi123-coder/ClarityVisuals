package ru.method.clarityvisuals.api.mixins;

import net.minecraft.class_1713;
import net.minecraft.class_1735;
import net.minecraft.class_332;
import net.minecraft.class_437;
import net.minecraft.class_465;
import net.minecraft.class_490;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.method.clarityvisuals.ClarityVisuals;
import ru.method.clarityvisuals.api.mixins.accessors.IHandledScreen;
import ru.method.clarityvisuals.modules.impl.client.AuctionHelperModule;
import ru.method.clarityvisuals.inventory.InventoryHelper;
import ru.method.clarityvisuals.manager.InventorySetupManager;
import ru.method.clarityvisuals.inventory.InventorySetupRow;
import ru.method.clarityvisuals.modules.impl.player.ItemScrollerModule;

@Mixin(value={class_465.class})
public abstract class HandledScreenMixin {
    @Shadow
    protected class_1735 field_2787;
    @Shadow
    protected int field_2776;
    @Shadow
    protected int field_2800;
    @Shadow
    protected int field_2792;

    @Inject(method={"render"}, at={@At(value="TAIL")})
    private void clarityvisuals$invOverlay(class_332 class_3322, int n, int n2, float f, CallbackInfo callbackInfo) {
        HandledScreenMixin handledScreenMixin = this;
        if (!((Object)handledScreenMixin instanceof class_490)) {
            return;
        }
        class_490 class_4902 = (class_490)(Object)handledScreenMixin;
        InventorySetupRow.a(class_3322, class_4902, this.field_2776, this.field_2800, this.field_2792, n, n2);
    }

    @Inject(method={"mouseClicked"}, at={@At(value="HEAD")}, cancellable=true)
    private void clarityvisuals$invOverlayClick(double d, double d2, int n, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        HandledScreenMixin handledScreenMixin = this;
        if (!((Object)handledScreenMixin instanceof class_490)) {
            return;
        }
        class_490 class_4902 = (class_490)(Object)handledScreenMixin;
        if (InventorySetupRow.a(class_4902, this.field_2776, this.field_2800, this.field_2792, d, d2, n)) {
            callbackInfoReturnable.setReturnValue(true);
        }
    }

    @Inject(method={"render"}, at={@At(value="TAIL")})
    private void clarityvisuals$maintainFrozenCursor(class_332 class_3322, int n, int n2, float f, CallbackInfo callbackInfo) {
        if (!InventoryHelper.rM()) {
            return;
        }
        InventoryHelper.d((class_465)(Object)this);
    }

    @Inject(method={"render"}, at={@At(value="HEAD")})
    private void clarityvisuals$auctionHelperPrepare(class_332 class_3322, int n, int n2, float f, CallbackInfo callbackInfo) {
        AuctionHelperModule auctionHelperModule = ClarityVisuals.getInstance().getModuleManager().getModule(AuctionHelperModule.class);
        if (auctionHelperModule == null || !auctionHelperModule.isToggle()) {
            return;
        }
        auctionHelperModule.a((class_465)(Object)this);
    }

    @Inject(method={"render"}, at={@At(value="TAIL")})
    private void clarityvisuals$auctionHelperOverlay(class_332 class_3322, int n, int n2, float f, CallbackInfo callbackInfo) {
        AuctionHelperModule auctionHelperModule = ClarityVisuals.getInstance().getModuleManager().getModule(AuctionHelperModule.class);
        if (auctionHelperModule == null || !auctionHelperModule.isToggle()) {
            return;
        }
        auctionHelperModule.b(class_3322, this.field_2776, this.field_2800);
    }

    @Inject(method={"mouseClicked"}, at={@At(value="HEAD")}, cancellable=true)
    private void clarityvisuals$beginItemScroller(double d, double d2, int n, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        ItemScrollerModule itemScrollerModule = ClarityVisuals.getInstance().getModuleManager().getModule(ItemScrollerModule.class);
        if (itemScrollerModule == null || !itemScrollerModule.isToggled()) {
            return;
        }
        ItemScrollerModule.ScrollDirection scrollDirection = itemScrollerModule.a(n, class_437.method_25442(), class_437.method_25441(), class_437.method_25443());
        if (scrollDirection == ItemScrollerModule.ScrollDirection.vn) {
            return;
        }
        class_1735 class_17352 = ((IHandledScreen)((Object)this)).invokeGetSlotAt(d, d2);
        if (itemScrollerModule.a((class_465)(Object)this, class_17352, d, d2, scrollDirection) || itemScrollerModule.bV()) {
            callbackInfoReturnable.setReturnValue(true);
        }
    }

    @Inject(method={"mouseDragged"}, at={@At(value="HEAD")}, cancellable=true)
    private void clarityvisuals$dragItemScroller(double d, double d2, int n, double d3, double d4, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        ItemScrollerModule itemScrollerModule = ClarityVisuals.getInstance().getModuleManager().getModule(ItemScrollerModule.class);
        if (itemScrollerModule == null || !itemScrollerModule.isToggled() || !itemScrollerModule.bV() || n != 0) {
            return;
        }
        class_1735 class_17352 = ((IHandledScreen)((Object)this)).invokeGetSlotAt(d, d2);
        itemScrollerModule.a((class_465)(Object)this, d, d2, class_17352);
        callbackInfoReturnable.setReturnValue(true);
    }

    @Inject(method={"mouseReleased"}, at={@At(value="HEAD")}, cancellable=true)
    private void clarityvisuals$releaseItemScroller(double d, double d2, int n, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        ItemScrollerModule itemScrollerModule = ClarityVisuals.getInstance().getModuleManager().getModule(ItemScrollerModule.class);
        if (itemScrollerModule == null || !itemScrollerModule.bV() || n != 0) {
            return;
        }
        itemScrollerModule.stopDragging();
        callbackInfoReturnable.setReturnValue(true);
    }

    @Inject(method={"mouseScrolled"}, at={@At(value="HEAD")}, cancellable=true)
    private void clarityvisuals$scrollItemScroller(double d, double d2, double d3, double d4, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        ItemScrollerModule itemScrollerModule = ClarityVisuals.getInstance().getModuleManager().getModule(ItemScrollerModule.class);
        if (itemScrollerModule == null || !itemScrollerModule.isToggled()) {
            return;
        }
        if (d4 == 0.0) {
            return;
        }
        class_1735 class_17352 = ((IHandledScreen)((Object)this)).invokeGetSlotAt(d, d2);
        if (itemScrollerModule.a((class_465)(Object)this, class_17352, d4 > 0.0, class_437.method_25442(), class_437.method_25441(), class_437.method_25443())) {
            callbackInfoReturnable.setReturnValue(true);
        }
    }

    @Inject(method={"close"}, at={@At(value="HEAD")})
    private void clarityvisuals$playerClosedInventory(CallbackInfo callbackInfo) {
        if ((Object)this instanceof class_490) {
            InventorySetupManager.mq();
        }
    }

    @Inject(method={"removed"}, at={@At(value="HEAD")})
    private void clarityvisuals$resetItemScrollerState(CallbackInfo callbackInfo) {
        ItemScrollerModule itemScrollerModule = ClarityVisuals.getInstance().getModuleManager().getModule(ItemScrollerModule.class);
        if (itemScrollerModule != null && itemScrollerModule.bV()) {
            itemScrollerModule.stopDragging();
        }
    }
}
