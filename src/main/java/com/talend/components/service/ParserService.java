package com.talend.components.service;

import org.talend.sdk.component.api.record.Record;
import org.talend.sdk.component.api.record.Schema;
import org.talend.sdk.component.api.service.Service;
import org.talend.sdk.component.api.service.completion.DynamicValues;
import org.talend.sdk.component.api.service.completion.SuggestionValues;
import org.talend.sdk.component.api.service.completion.Suggestions;
import org.talend.sdk.component.api.service.completion.Values;

import java.time.ZonedDateTime;
import java.util.*;

@Service
public class ParserService {

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

    // copy from component runtine impl
    public boolean forwardEntry(final Record source, final Record.Builder builder, final String sourceColumn,
                                final Schema.Entry entry) {
        switch (entry.getType()) {
            case INT:
                final OptionalInt optionalInt = source.getOptionalInt(sourceColumn);
                optionalInt.ifPresent(v -> builder.withInt(entry, v));
                return optionalInt.isPresent();
            case LONG:
                final OptionalLong optionalLong = source.getOptionalLong(sourceColumn);
                optionalLong.ifPresent(v -> builder.withLong(entry, v));
                return optionalLong.isPresent();
            case FLOAT:
                final OptionalDouble optionalFloat = source.getOptionalFloat(sourceColumn);
                optionalFloat.ifPresent(v -> builder.withFloat(entry, (float) v));
                return optionalFloat.isPresent();
            case DOUBLE:
                final OptionalDouble optionalDouble = source.getOptionalDouble(sourceColumn);
                optionalDouble.ifPresent(v -> builder.withDouble(entry, v));
                return optionalDouble.isPresent();
            case BOOLEAN:
                final Optional<Boolean> optionalBoolean = source.getOptionalBoolean(sourceColumn);
                optionalBoolean.ifPresent(v -> builder.withBoolean(entry, v));
                return optionalBoolean.isPresent();
            case STRING:
                final Optional<String> optionalString = source.getOptionalString(sourceColumn);
                optionalString.ifPresent(v -> builder.withString(entry, v));
                return optionalString.isPresent();
            case DATETIME:
                final Optional<ZonedDateTime> optionalDateTime = source.getOptionalDateTime(sourceColumn);
                optionalDateTime.ifPresent(v -> builder.withDateTime(entry, v));
                return optionalDateTime.isPresent();
            case BYTES:
                final Optional<byte[]> optionalBytes = source.getOptionalBytes(sourceColumn);
                optionalBytes.ifPresent(v -> builder.withBytes(entry, v));
                return optionalBytes.isPresent();
            case RECORD:
                final Optional<Record> optionalRecord = source.getOptionalRecord(sourceColumn);
                optionalRecord.ifPresent(v -> builder.withRecord(entry, v));
                return optionalRecord.isPresent();
            case ARRAY:
                final Optional<Collection<Object>> optionalArray = source.getOptionalArray(Object.class, sourceColumn);
                optionalArray.ifPresent(v -> builder.withArray(entry, v));
                return optionalArray.isPresent();
            default:
                throw new IllegalStateException("Unsupported entry type: " + entry);
        }
    }

}