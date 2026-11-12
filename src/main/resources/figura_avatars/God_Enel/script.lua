vanilla_model.PLAYER:setVisible(false)

--░░░░░░░▒▒▒▒▒▒▒▓▓▓▓▓▓▓███████ 𝐀𝐂𝐓𝐈𝐎𝐍 𝐖𝐇𝐄𝐄𝐋 ███████▓▓▓▓▓▓▓▒▒▒▒▒▒▒░░░░░░░ BY @king_sybbyl

--=============================================================================
-- Create the main action wheel page
local mainPage = action_wheel:newPage()
action_wheel:setPage(mainPage)

-- Create the texture selection page
local texturepage = action_wheel:newPage()

-- Add a back button to the texture selection page
texturepage:newAction()
    :title("Back")
    :item("arrow")
    :onLeftClick(function()
        action_wheel:setPage(mainPage)
    end)


--█████████████████████████████████████████████████████████████████████████████

--=============================================================================
-- Function to toggle the shocked face visibility
:onToggle(pings.shock)
function pings.shock(state)
    models.enel.Head.shock:setVisible(state)
    models.enel.Head.normal:setVisible(not state)
end


-- Add action to the main page for shock toggle
local actyShock = mainPage:newAction()
    :title("Shocked Toggle")
    :item("lightning_rod")
    :onToggle(pings.shock)
    --=============================================================================

-- Function to switch to the texture selection page
function switchToTexturePage()
    action_wheel:setPage(texturepage)
end
