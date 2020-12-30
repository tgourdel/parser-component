package com.talend.components.processor;

import static org.talend.sdk.component.api.configuration.condition.ActiveIfs.Operator.AND;

import com.talend.components.service.Format;
import lombok.Data;
import org.talend.sdk.component.api.configuration.Option;
import org.talend.sdk.component.api.configuration.action.Suggestable;
import org.talend.sdk.component.api.configuration.condition.ActiveIf;
import org.talend.sdk.component.api.configuration.condition.ActiveIfs;
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
        @GridLayout.Row({ "field"}),
        @GridLayout.Row({ "enforceString"})
})
@Documentation("JSON or XML parser on input fields.")
public class ParserConfiguration implements Serializable {

    @Option
    @Documentation("Selection mode")
    @Suggestable("LABEL_TOGGLE")
    private SelectionMode selectionMode = SelectionMode.SIMPLE;

    @Option
    @Required
    @ActiveIf(target = "selectionMode", value = {"SIMPLE", "ADVANCED"})
    @Suggestable(INCOMING_PATHS_DYNAMIC)
    @Documentation("The input field name")
    private String field = "";

    @Option
    @Required
    @DefaultValue("JSON")
    @ActiveIf(target = "selectionMode", value = {"SIMPLE", "ADVANCED"})
    @Documentation("types")
    private Format format;

    @Option
    @Required
    @DefaultValue("false")
    @ActiveIfs(operator = AND, value = { @ActiveIf(target = "selectionMode", value = "ADVANCED"),
            @ActiveIf(target = "format", value = "XML") })
    @Documentation("Enforce String")
    private boolean enforceString = false;

    @Option
    @Required
    @DefaultValue("false")
    @ActiveIfs(operator = AND, value = { @ActiveIf(target = "selectionMode", value = "ADVANCED"),
            @ActiveIf(target = "format", value = "JSON") })
    @Documentation("Force number as double")
    private boolean forceDouble = false;

    public enum SelectionMode {
        SIMPLE,
        ADVANCED
    }

}