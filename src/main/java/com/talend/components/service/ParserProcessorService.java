package com.talend.components.service;

import org.talend.sdk.component.api.record.Record;
import org.talend.sdk.component.api.record.Schema;
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

    public Record.Builder copyRecordExceptOneField(Record inputRecord, Record.Builder builder, String fieldToRemove) {

        Schema inputSchema = inputRecord.getSchema();

        for (Schema.Entry entry : inputSchema.getEntries())
        {
            if(!entry.getName().equals(fieldToRemove)) {
                switch (entry.getType()) {
                    case DATETIME:
                        builder.withDateTime(entry.getName(), inputRecord.getDateTime(entry.getName()));
                        break;
                    case BOOLEAN:
                        builder.withBoolean(entry.getName(), inputRecord.getBoolean(entry.getName()));
                        break;
                    case DOUBLE:
                        builder.withDouble(entry.getName(), inputRecord.getDouble(entry.getName()));
                        break;
                    case INT:
                        builder.withInt(entry.getName(), inputRecord.getInt(entry.getName()));
                        break;
                    case LONG:
                        builder.withLong(entry.getName(), inputRecord.getLong(entry.getName()));
                        break;
                    case FLOAT:
                        builder.withFloat(entry.getName(), inputRecord.getFloat(entry.getName()));
                        break;
                    case STRING:
                        builder.withString(entry.getName(), inputRecord.getString(entry.getName()));
                        break;
                    case BYTES:
                        builder.withBytes(entry.getName(), inputRecord.getBytes(entry.getName()));
                        break;
                    case ARRAY:
                        // builder.withArray(entry.getName(), inputRecord.getArray(Class<entry.getElementSchema().getType()>, entry.getName()));
                        break;
                    case RECORD:
                        builder.withRecord(entry.getName(), inputRecord.getRecord(entry.getName()));
                }
            }
        }
        return builder;
    }

}