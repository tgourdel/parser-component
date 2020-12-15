package com.talend.components.processor;

import com.talend.components.service.Format;
import lombok.Data;
import org.talend.sdk.component.api.configuration.Option;
import org.talend.sdk.component.api.configuration.action.Suggestable;
import org.talend.sdk.component.api.configuration.constraint.Required;
import org.talend.sdk.component.api.configuration.ui.DefaultValue;
import org.talend.sdk.component.api.configuration.ui.layout.GridLayout;
import org.talend.sdk.component.api.meta.Documentation;

import java.io.Serializable;

import static com.talend.components.service.ParserComponentService.INCOMING_PATHS_DYNAMIC;

@Data
@GridLayout({
        @GridLayout.Row({ "field"}),
        @GridLayout.Row({ "format"})
})
@Documentation("Parses data")
public class ParserProcessorConfiguration implements Serializable {

    @Option
    @Required
    @Suggestable(INCOMING_PATHS_DYNAMIC)
    @Documentation("The input field name")
    private String field = "";

    @Option
    @Required
    @DefaultValue("JSON")
    @Documentation("types")
    private Format format;

}