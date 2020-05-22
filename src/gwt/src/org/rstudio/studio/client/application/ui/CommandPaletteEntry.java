/*
 * CommandPaletteEntry.java
 *
 * Copyright (C) 2020 by RStudio, PBC
 *
 * Unless you have received this program directly from RStudio pursuant
 * to the terms of a commercial license agreement with RStudio, then
 * this program is licensed to you under the terms of version 3 of the
 * GNU Affero General Public License. This program is distributed WITHOUT
 * ANY EXPRESS OR IMPLIED WARRANTY, INCLUDING THOSE OF NON-INFRINGEMENT,
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE. Please refer to the
 * AGPL (http://www.gnu.org/licenses/agpl-3.0.txt) for more details.
 *
 */
package org.rstudio.studio.client.application.ui;

import org.rstudio.core.client.ElementIds;
import org.rstudio.core.client.SafeHtmlUtil;
import org.rstudio.core.client.StringUtil;

import com.google.gwt.aria.client.Roles;
import com.google.gwt.aria.client.SelectedValue;
import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public abstract class CommandPaletteEntry extends Composite
{
   private static CommandPaletteEntryUiBinder uiBinder = GWT
         .create(CommandPaletteEntryUiBinder.class);

   interface CommandPaletteEntryUiBinder extends UiBinder<Widget, CommandPaletteEntry>
   {
   }

   public interface Styles extends CssResource
   {
      String entry();
      String keyboard();
      String searchMatch();
      String selected();
      String name();
      String disabled();
   }

   public CommandPaletteEntry()
   {
      initWidget(uiBinder.createAndBindUi(this));
      selected_ = false;
      
      Roles.getOptionRole().set(getElement());
      Roles.getOptionRole().setAriaSelectedState(getElement(), SelectedValue.FALSE);
   }

   public void initialize()
   {
      String id = getId();
      if (id != null)
      {
         // Assign a unique element ID (for accessibility tree). There's no need
         // to do this if there's no ID as we'll ultimately discard widgets
         // which don't have an addressable ID.
         ElementIds.assignElementId(getElement(), ElementIds.COMMAND_ENTRY_PREFIX + 
               "_" + getScope() + "_" +
               ElementIds.idSafeString(id));
      }

      // Apply command label
      name_.setText(getLabel());
      
      // If the command is not enabled, style it as disabled.
      if (!enabled())
      {
         addStyleName(styles_.disabled());
         Roles.getOptionRole().setAriaDisabledState(getElement(), true);
      }

      // Apply context
      String context = getContext();
      if (StringUtil.isNullOrEmpty(context))
      {
         context_.setVisible(false);
      }
      else
      {
         context_.getElement().setInnerHTML(context);
         context_.setVisible(true);
      }
      
      // Insert invoker
      invoker_.add(getInvoker());
   }
   
   /*
    * Set whether or not the command should appear selected.
    */
   public void setSelected(boolean selected)
   {
      // No-op if we're not changing state
      if (selected_ == selected)
         return;
      
      // Add the CSS class indicating that this entry is selected
      if (selected)
         addStyleName(styles_.selected());
      else
         removeStyleName(styles_.selected());

      // Update ARIA state to indicate that we're selected. (The host is
      // responsible for updating other ARIA state such as active descendant.)
      Roles.getOptionRole().setAriaSelectedState(getElement(), 
            selected ? SelectedValue.TRUE : SelectedValue.FALSE);
      
      selected_ = selected;
   }
   
   /**
    * Highlights the given keywords on the command entry.
    */
   public void setSearchHighlight(String[] keywords)
   {
      if (keywords.length == 0)
      {
         name_.setText(getLabel());
      }
      else
      {
         SafeHtmlBuilder sb = new SafeHtmlBuilder();
         SafeHtmlUtil.highlightSearchMatch(sb, getLabel(), keywords, 
               styles_.searchMatch());
         name_.getElement().setInnerSafeHtml(sb.toSafeHtml());
      }
   }
   
   /**
    * Get the label for the entry.
    * 
    * @return The entry's label.
    */
   abstract public String getLabel();

   /**
    * Invoke the entry (execute the command, etc.)
    */
   abstract public void invoke();

   /**
    * Get the entry's ID.
    * 
    * @return A unique ID referring to the entry.
    */
   abstract public String getId();
   
   /**
    * Get the entry's scope. This is not displayed to users; it is a short
    * alphanumeric string used to ensure IDs are unique across different kinds
    * of entries which may have their own ID systems.
    * 
    * @return The entry's scope.
    */
   abstract public String getScope();

   /**
    * Get the entry's context. This is displayed to the user to help
    * disambiguate similar-looking entries.
    * 
    * @return The entry's context.
    */
   abstract public String getContext();

   /**
    * Is the entry currently enabled?
    * 
    * @return Whether the entry is enabled.
    */
   abstract public boolean enabled();

   /**
    * Get a widget that can be used to invoke the entry.
    * 
    * @return A widget to invoke the entry.
    */
   abstract public Widget getInvoker();
   
   /**
    * Dismiss after invoke?
    * 
    * @return Whether to dismiss the palette after invoking the entry.
    */
   abstract public boolean dismissOnInvoke();

   private boolean selected_;

   @UiField public Label context_;
   @UiField public Label name_;
   @UiField public HTMLPanel invoker_;
   @UiField public Styles styles_;
}