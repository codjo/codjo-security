package net.codjo.security.gui.user;
import net.codjo.test.common.LogString;
/**
 *
 */
class GuiResultHandlerMock implements GuiResultHandler {
    private final LogString log;


    GuiResultHandlerMock(LogString log) {
        this.log = log;
    }


    public void handleCancel() {
        log.call("handleCancel");
    }


    public void handleValidate() {
        log.call("handleValidate");
    }
}
