package net.codjo.security.server.plugin;
import java.io.File;
import net.codjo.security.common.message.DefaultModelManager;
import net.codjo.security.common.message.ModelManager;
import net.codjo.test.common.LogString;
/**
 *
 */
public class ModelManagerFileLoaderMock extends ModelManagerFileLoader {
    private final LogString log;
    private ModelManager model = new DefaultModelManager();


    public ModelManagerFileLoaderMock(LogString log) {
        super(new File("."));
        this.log = log;
    }


    @Override
    public ModelManager load() {
        log.call("load");
        return model;
    }


    public ModelManagerFileLoaderMock mockLoad(ModelManager modelManager) {
        model = modelManager;
        return this;
    }
}
