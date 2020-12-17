package com.talend.components.service;

import org.talend.sdk.component.api.service.Service;
import org.talend.sdk.component.api.service.completion.DynamicValues;
import org.talend.sdk.component.api.service.completion.SuggestionValues;
import org.talend.sdk.component.api.service.completion.Suggestions;
import org.talend.sdk.component.api.service.completion.Values;

import java.util.Collections;

@Service
public class ParserProcessorService {

    public static final String INCOMING_PATHS_DYNAMIC = "INCOMING_PATHS_DYNAMIC";

    public static final String TREEVIEW_RENAME_INCOMING_PATHS_DYNAMIC = "TREEVIEW_RENAME_INCOMING_PATHS_DYNAMIC";

    public static final String LABEL_TOGGLE = "LABEL_TOGGLE";

    // This INCOMING_PATHS_DYNAMIC service is a flag for inject incoming paths dynamic, won't be called
    @Suggestions(INCOMING_PATHS_DYNAMIC)
    public SuggestionValues leftPath() {
        return new SuggestionValues(false, Collections.EMPTY_LIST);
    }

    // This INCOMING_PATHS_DYNAMIC service is a flag for inject incoming paths dynamic, won't be called
    @DynamicValues(INCOMING_PATHS_DYNAMIC)
    public Values actions() {
        return new Values(Collections.EMPTY_LIST);
    }

    // This TREEVIEW_RENAME_INCOMING_PATHS_DYNAMIC service is a flag for inject incoming paths dynamic, won't be called
    @Suggestions(TREEVIEW_RENAME_INCOMING_PATHS_DYNAMIC)
    public SuggestionValues treeviewRenameSuggestionValues() {
        return new SuggestionValues(false, Collections.EMPTY_LIST);
    }

    // This TREEVIEW_RENAME_INCOMING_PATHS_DYNAMIC service is a flag for inject incoming paths dynamic, won't be called
    @Suggestions(LABEL_TOGGLE)
    public SuggestionValues labelToggleSuggestionValues() {
        return new SuggestionValues(false, Collections.EMPTY_LIST);
    }

}