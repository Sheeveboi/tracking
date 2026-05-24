package net.altosheeve.tracking.client.Soprano;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ScrollableWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;

public class Terminal extends Screen {

    //TODO: we might want more terminals open at once. Executing threads should have a reference to a terminal object instead of just adding to a static ArrayList
    public static ArrayList<String> lines = new ArrayList<>();

    public static int lineMargin = 12;
    public static int scrollOffset = 0;

    public static int lastKeycode = 0;

    public static TextFieldWidget input;

    public Terminal(Text title) {
        super(title);
    }

    /*@Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {

        lastKeycode = keyCode;

        switch (keyCode) {

            case 257: //enter

                String command = input.getText();

                System.out.println(command);

                Shell kernel = new Shell(new ArrayList<>(), new ArrayList<>(), null);
                try {
                    System.out.println(Shell.terminalImplementation.runCompiler(command));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

                try {
                    kernel.runCommand(command);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }*/

    @Override
    public void init() {

        input = new TextFieldWidget(this.textRenderer, 0, this.height - 20, this.width, 20, Text.of("Node X"));

        ScrollableWidget clickRegion = new ScrollableWidget(
                0, 0,
                this.client.getWindow().getWidth(), this.client.getWindow().getHeight(),
                Text.of("")
        ) {
            @Override
            protected int getContentsHeightWithPadding() {
                return 0;
            }

            @Override
            protected double getDeltaYPerScroll() {
                return lineMargin;
            }

            @Override
            protected void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
            }

            @Override
            protected void appendClickableNarrations(NarrationMessageBuilder builder) {

            }

            /*@Override
            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                return false;
            }

            @Override
            public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
                if (!hasShiftDown() && !hasAltDown() && !hasControlDown()) {
                    scrollOffset += (int) (lineMargin * verticalAmount);
                }
                return true;
            }*/
        };

        addDrawableChild(input);
        addDrawableChild(clickRegion);

    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {

        int offset = this.height - (lines.size() * lineMargin);

        for (int i = 0; i < lines.size(); i++) {
            context.drawText(this.textRenderer, lines.get(i), 20, offset + (i * lineMargin) + scrollOffset - 20, 0xffffffff, true);
        }

        super.render(context, mouseX, mouseY, delta);

        context.drawText(this.textRenderer, "Last keycode: " + lastKeycode, this.width - 100, this.height - 30, 0xffffffff, true);
    }

}
