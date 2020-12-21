package com.talend.components.processor;

import com.talend.components.service.Format;
import lombok.Data;
import org.talend.sdk.component.api.configuration.Option;
import org.talend.sdk.component.api.configuration.action.Suggestable;
import org.talend.sdk.component.api.configuration.condition.ActiveIf;
import org.talend.sdk.component.api.configuration.constraint.Required;
import org.talend.sdk.component.api.configuration.ui.DefaultValue;
import org.talend.sdk.component.api.configuration.ui.layout.GridLayout;
import org.talend.sdk.component.api.configuration.ui.OptionsOrder;

import org.talend.sdk.component.api.meta.Documentation;

import java.io.Serializable;

import static com.talend.components.service.ParserService.INCOMING_PATHS_DYNAMIC;

@Data
@GridLayout({
        @GridLayout.Row({ "selectionMode"}),
        @GridLayout.Row({ "format"}),
        @GridLayout.Row({ "field"})
})
@Documentation("JSON or XML parser on input fields.")
public class ParserConfiguration implements Serializable {

    @Option
    @Documentation("Selection mode")
    @Suggestable("LABEL_TOGGLE")
    private SelectionMode selectionMode = SelectionMode.SIMPLE;

    @Option
    @Required
    @ActiveIf(target = "selectionMode", value = "SIMPLE")
    @Suggestable(INCOMING_PATHS_DYNAMIC)
    @Documentation("The input field name")
    private String field = "";

    @Option
    @Required
    @DefaultValue("JSON")
    @ActiveIf(target = "selectionMode", value = "ADVANCED")
    @Documentation("types")
    private Format format;


    public enum SelectionMode {
        SIMPLE,
        ADVANCED
    }

}