vanilla_model.ARMOR:setVisible(false)
vanilla_model.CAPE:setVisible(false)
vanilla_model.ELYTRA:setVisible(false)
vanilla_model.PLAYER:setVisible(false)
nameplate.ENTITY:setPos(0, .7, 0)

local physBone = require("physBoneAPI")

physBone:setPreset(
    "physBoob",
    2,
    nil,
    -5,
    0.85,
    1,
    vec(0, 0, -1),
    140,
    nil,
    vec(-90, 0, 0),
    nil,
    nil,
    nil,
    nil,
    nil,
    nil,
    0.05
)

physBone:setPreset(
    "PhysBoob",
    2,
    nil,
    -5,
    0.85,
    1,
    vec(0, 0, -1),
    140,
    nil,
    vec(-90, 0, 0),
    nil,
    nil,
    nil,
    nil,
    nil,
    nil,
    0.05
)

local capeParts = {
    models.model.root.Body.Jacket,
    models.model.root.Body.Jacket.LeftSleeve,
}
local tentacleWaveParts = {}
local tentacleWaveRegistered = false
local capeRot = vec(0, 0, 0)
local prevCapeRot = vec(0, 0, 0)
local prevYaw = 0
local bobt = 0
local physicsEnabled = true
local physicsAction
local leftItemAnimPlaying = false
local rightItemAnimPlaying = false
local rightItemPhysBonesEnabled = true
local rightItemPhysBoneIDs = {
    "physBoobRightArmTentacle1",
    "physBoobRightArmTentacle2",
    "physBoobRightArmTentacle3",
}
local rightItemPhysBoneLookup = {}
local rightItemPhysBoneParts = {}
local physBoneDebugPartNames = {
    PB_Debug_Pivot = true,
    PB_Debug_Direction = true,
    PB_Debug_SpringForce = true,
    PB_Debug_NodeRadius = true,
}

for _, physBoneID in ipairs(rightItemPhysBoneIDs) do
    rightItemPhysBoneLookup[physBoneID] = true
end

local setRightItemPhysBonesEnabled

local function isItemStackEmpty(stack)
    if not stack then
        return true
    end

    local ok, empty = pcall(function()
        return stack:isEmpty()
    end)

    if ok then
        return empty
    end

    return stack.id == "minecraft:air" or stack.id == nil
end

local function getHeldItem(offhand)
    local ok, stack = pcall(function()
        return player:getHeldItem(offhand)
    end)

    if ok then
        return stack
    end
end

local function isLeftHanded()
    local ok, leftHanded = pcall(function()
        return player:isLeftHanded()
    end)

    return ok and leftHanded
end

local function setAnimationPlaying(animation, playing)
    if not animation then
        return
    end

    if playing then
        animation:play()
    else
        animation:stop()
    end
end

local function hidePhysBoneDebugParts(part)
    if not part then
        return
    end

    if physBoneDebugPartNames[part:getName()] then
        part:setVisible(false)
    end

    for debugPartName in pairs(physBoneDebugPartNames) do
        if part[debugPartName] then
            part[debugPartName]:setVisible(false)
        end
    end

    for _, child in ipairs(part:getChildren()) do
        hidePhysBoneDebugParts(child)
    end
end

local function resetPhysBoneChildren(physBoneRoot)
    for _, child in ipairs(physBoneRoot.path:getChildren()) do
        if physBoneDebugPartNames[child:getName()] then
            child:setVisible(false)
        else
            child:setMatrix(matrices.mat4())
        end
    end
end

local function setPhysBoneRenderingEnabled(enabled)
    for _, boneID in pairs(physBone.index) do
        local bone = physBone[boneID]

        if bone then
            if enabled then
                bone.path.midRender = function(delta, context)
                    if physicsEnabled then
                        physBone.physBoneRender(delta, context, boneID)
                    end
                end
            else
                bone.velocity = vec(0, 0, 0)
                bone.pos = bone.path:partToWorldMatrix():apply()
                bone.path.midRender = function() end
            end
        end
    end
end

local function updatePhysicsAction()
    if not physicsAction then
        return
    end

    if physicsEnabled then
        physicsAction:title("Disable Physics")
        physicsAction:item("minecraft:redstone")
    else
        physicsAction:title("Enable Physics")
        physicsAction:item("minecraft:slime_ball")
    end
end

local function setPhysicsEnabled(enabled)
    if physicsEnabled == enabled then
        return
    end

    physicsEnabled = enabled
    setPhysBoneRenderingEnabled(enabled)

    if enabled then
        setRightItemPhysBonesEnabled(not rightItemAnimPlaying)
    end

    if not enabled then
        updatePhysicsAction()
        return
    end

    updatePhysicsAction()
end

local function setupActionWheel()
    if not action_wheel then
        return
    end

    local page = action_wheel:newPage()
    physicsAction = page:newAction()
        :title("Disable Physics")
        :item("minecraft:redstone")
        :onLeftClick(function()
            setPhysicsEnabled(not physicsEnabled)
        end)

    action_wheel:setPage(page)
    updatePhysicsAction()
end

function setRightItemPhysBonesEnabled(enabled)
    if rightItemPhysBonesEnabled == enabled then
        return
    end

    rightItemPhysBonesEnabled = enabled

    if enabled then
        for _, physBoneID in ipairs(rightItemPhysBoneIDs) do
            local part = rightItemPhysBoneParts[physBoneID]

            if part and not physBone[physBoneID] then
                local ok, bone = pcall(function()
                    return physBone:newPhysBone(part, "physBoob")
                end)

                if ok and bone then
                    bone
                        :setSpringForce(140)
                        :setAirResistance(0.85)
                        :setGravity(-5)
                        :setBounce(0.05)
                    hidePhysBoneDebugParts(part)
                end
            end
        end
    else
        for i = #rightItemPhysBoneIDs, 1, -1 do
            local physBoneID = rightItemPhysBoneIDs[i]
            local bone = physBone[physBoneID]

            if bone then
                rightItemPhysBoneParts[physBoneID] = bone.path
                hidePhysBoneDebugParts(bone.path)
                bone.path.midRender = function() end
                resetPhysBoneChildren(bone)
                bone:remove()
                hidePhysBoneDebugParts(rightItemPhysBoneParts[physBoneID])
            end
        end
    end
end

local function collectTentacleWaveParts(part)
    local name = part:getName()

    if name:find("Tentacle") and (name:find("^physBoob") or name:find("^PhysBoob")) then
        local segment = tonumber(name:match("Tentacle(%d+)")) or 1

        local side = name:find("Left") and -1 or 1
        local phaseOffset = (#tentacleWaveParts * 1.37 + segment * 0.41) % 6.28318
        local speedOffset = 0.75 + ((#tentacleWaveParts * 37 + segment * 11) % 45) / 100

        for _, child in ipairs(part:getChildren()) do
            local childName = child:getName()

            if not (childName:find("^physBoob") or childName:find("^PhysBoob")) then
                tentacleWaveParts[#tentacleWaveParts + 1] = {
                    part = child,
                    rootName = name,
                    baseRot = child:getRot(),
                    segment = segment,
                    side = side,
                    phaseOffset = phaseOffset,
                    speedOffset = speedOffset,
                }
            end
        end
    end

    for _, child in ipairs(part:getChildren()) do
        collectTentacleWaveParts(child)
    end
end

events.entity_init:register(function()
    for _, boneID in pairs(physBone.index) do
        physBone[boneID]
            :setSpringForce(140)
            :setAirResistance(0.85)
            :setGravity(-5)
            :setBounce(0.05)
    end

    capeParts[1]:setParentType("Cape")
    capeParts[1]:setVisible(true)
    capeParts[2]:setVisible(true)
    hidePhysBoneDebugParts(models.model.root)
    setupActionWheel()

    tentacleWaveParts = {}
    collectTentacleWaveParts(models.model.root)

    if not tentacleWaveRegistered then
        tentacleWaveRegistered = true
        events.render:register(function(delta)
            if not physicsEnabled then
                return
            end

            local time = world.getTime() + delta

            for _, wavePart in ipairs(tentacleWaveParts) do
                if not rightItemPhysBonesEnabled and rightItemPhysBoneLookup[wavePart.rootName] then
                    wavePart.part:setRot(wavePart.baseRot)
                else
                    local phase = time * 0.16 * wavePart.speedOffset + wavePart.segment * 0.72 + wavePart.phaseOffset
                    local strength = math.min(wavePart.segment, 4)
                    local x = math.sin(phase) * 2.5 * strength
                    local y = math.sin(phase * 0.83 + wavePart.phaseOffset * 1.7) * 1 * strength
                    local z = math.cos(phase * 1.11 + wavePart.phaseOffset * 0.6) * 1.75 * strength

                    local baseRot = wavePart.baseRot
                    wavePart.part:setRot(baseRot.x + x, baseRot.y + y, baseRot.z + z)
                end
            end
        end, "DavyJones.tentacleWave")
    end

    prevYaw = player:getBodyYaw() + 90
end, "DavyJones.stiffenPhysBones")

events.render:register(function()
    hidePhysBoneDebugParts(models.model.root)
end, "DavyJones.hidePhysBoneDebugParts")

events.tick:register(function()
    local mainhandHasItem = not isItemStackEmpty(getHeldItem(false))
    local offhandHasItem = not isItemStackEmpty(getHeldItem(true))
    local leftHasItem = isLeftHanded() and mainhandHasItem or offhandHasItem
    local rightHasItem = isLeftHanded() and offhandHasItem or mainhandHasItem

    if leftHasItem ~= leftItemAnimPlaying then
        leftItemAnimPlaying = leftHasItem
        setAnimationPlaying(animations.model.leftitem, leftHasItem)
    end

    if rightHasItem ~= rightItemAnimPlaying then
        rightItemAnimPlaying = rightHasItem
        setAnimationPlaying(animations.model.rightitem, rightHasItem)

        if rightHasItem then
            setRightItemPhysBonesEnabled(false)
        elseif physicsEnabled then
            setRightItemPhysBonesEnabled(true)
        end
    end

    if not physicsEnabled then
        return
    end

    local velocity = player:getVelocity()
    local y = math.max(velocity.y, -1)
    velocity.y = 0

    local length = velocity:length()
    bobt = bobt + length * 3.5

    local bobbing = math.sin(bobt) * length * 40 * (player:isOnGround() and 1 or 0)
    local yaw = player:getBodyYaw() + 90
    local dir = vec(math.cos(math.rad(yaw)), 0, math.sin(math.rad(yaw)))
    local diff = math.clamp((dir:copy():cross(velocity) * 300).y + (yaw - prevYaw) * length * 13, -10, 10)

    local x = bobbing + math.min(0, math.max(-80, -6 - length * math.sign(velocity:dot(dir)) * 160) + y * 31 + math.abs(diff))
    local xz = math.lerp(capeRot.y, diff, 0.3)

    prevCapeRot = capeRot
    capeRot = vec(math.lerp(capeRot.x, x, 0.3), xz, xz)
    prevYaw = yaw
end, "DavyJones.capeDampingPrecompute")

events.render:register(function(delta)
    if not physicsEnabled then
        return
    end

    local capeCounterRot = math.lerp(prevCapeRot, capeRot, delta) * -0.45
    local armLrot = vanilla_model.LEFT_ARM:getOriginRot()
    local armBackPush = player:getPose() == "SWIMMING" and 0 or math.clamp(armLrot.x, -180, 0)

    capeParts[1]:setOffsetRot(capeCounterRot.x + armBackPush * 0.65, capeCounterRot.y, capeCounterRot.z)
    capeParts[2]:setOffsetRot(armBackPush * 0.35, 0, 0)
end, "DavyJones.capeDampingRender")
