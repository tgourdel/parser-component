// this tells the framework in which family (group of components) and categories (UI grouping)
// the components in the nested packages belong to
@Components(family = "parser", categories = "Misc")
@Icon(value = CUSTOM, custom = "parser") // icon is located at src/main/resources/icons/parser.svg
package com.talend.components;

import static org.talend.sdk.component.api.component.Icon.IconType.CUSTOM;

import org.talend.sdk.component.api.component.Components;
import org.talend.sdk.component.api.component.Icon;