package net.codjo.security.common.message;
/**
 *
 */
public class MessageBody {
    private ModelManager model;
    private SecurityEngineConfiguration configuration;


    public MessageBody(ModelManager model,
                       SecurityEngineConfiguration configuration) {
        this.model = model;
        this.configuration = configuration;
    }


    public ModelManager getModel() {
        return model;
    }


    public SecurityEngineConfiguration getConfiguration() {
        return configuration;
    }
}
