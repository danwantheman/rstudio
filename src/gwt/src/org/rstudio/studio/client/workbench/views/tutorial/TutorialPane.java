/*
 * TutorialPane.java
 *
 * Copyright (C) 2009-19 by RStudio, Inc.
 *
 * This program is licensed to you under the terms of version 3 of the
 * GNU Affero General Public License. This program is distributed WITHOUT
 * ANY EXPRESS OR IMPLIED WARRANTY, INCLUDING THOSE OF NON-INFRINGEMENT,
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE. Please refer to the
 * AGPL (http://www.gnu.org/licenses/agpl-3.0.txt) for more details.
 *
 */
package org.rstudio.studio.client.workbench.views.tutorial;

import org.rstudio.core.client.ElementIds;
import org.rstudio.core.client.StringUtil;
import org.rstudio.core.client.URIBuilder;
import org.rstudio.core.client.URIConstants;
import org.rstudio.core.client.URIUtils;
import org.rstudio.core.client.dom.WindowEx;
import org.rstudio.core.client.widget.RStudioFrame;
import org.rstudio.core.client.widget.Toolbar;
import org.rstudio.studio.client.application.Desktop;
import org.rstudio.studio.client.common.AutoGlassPanel;
import org.rstudio.studio.client.common.GlobalDisplay;
import org.rstudio.studio.client.common.GlobalDisplay.NewWindowOptions;
import org.rstudio.studio.client.shiny.model.ShinyApplicationParams;
import org.rstudio.studio.client.workbench.commands.Commands;
import org.rstudio.studio.client.workbench.ui.WorkbenchPane;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class TutorialPane
      extends WorkbenchPane
      implements TutorialPresenter.Display
{
   @Inject
   protected TutorialPane(GlobalDisplay globalDisplay,
                          Commands commands)
   {
      super("Tutorial");
      
      globalDisplay_ = globalDisplay;
      commands_ = commands;
      
      ensureWidget();
   }

   @Override
   protected Widget createMainWidget()
   {
      frame_ = new RStudioFrame("Tutorial Pane");
      frame_.setSize("100%", "100%");
      frame_.addStyleName("ace_editor_theme");
      frame_.setUrl(URIConstants.ABOUT_BLANK);
      ElementIds.assignElementId(frame_.getElement(), ElementIds.TUTORIAL_FRAME);
      
      showLandingPage();
      return new AutoGlassPanel(frame_);
   }
   
   @Override
   protected Toolbar createMainToolbar()
   {
      toolbar_ = new Toolbar("Tutorial Tab");
      toolbar_.addLeftWidget(commands_.tutorialPopout().createToolbarButton());
      toolbar_.addLeftWidget(commands_.tutorialStop().createToolbarButton());
      toolbar_.addRightWidget(commands_.tutorialRefresh().createToolbarButton());
      return toolbar_;
   }
   
   @Override
   public void back()
   {
      frame_.getWindow().back();
   }
   
   @Override
   public void forward()
   {
      frame_.getWindow().forward();
   }
   
   @Override
   public void popout()
   {
      WindowEx window = frame_.getWindow();
      String href = window.getLocationHref();
      NewWindowOptions options = new NewWindowOptions();
      
      int width = Math.max(800, window.getInnerWidth());
      int height = Math.max(800, window.getInnerHeight());
      
      globalDisplay_.openWebMinimalWindow(
            href,
            false,
            width,
            height,
            options);
   }
   
   @Override
   public void refresh()
   {
      frame_.getWindow().reload();
   }
   
   @Override
   public void openTutorial(ShinyApplicationParams params)
   {
      commands_.tutorialStop().setVisible(true);
      commands_.tutorialStop().setEnabled(true);
      navigate(params.getUrl(), true);
   }
   
   @Override
   public void showLandingPage()
   {
      String url = GWT.getHostPageBaseURL() + "tutorial/home/";
      frame_.setUrl(url);
   }
   
   private void navigate(String url, boolean useRawURL)
   {
      // save the unmodified URL for pop-out
      baseUrl_ = url;
      
      // in desktop mode we need to be careful about loading URLs which are
      // non-local; before changing the URL, set the iframe to be sandboxed
      // based on whether we're working with a local URL (note that prior to
      // RStudio 1.2 local URLs were forbidden entirely)
      if (Desktop.hasDesktopFrame())
      {
         if (URIUtils.isLocalUrl(url))
         {
            frame_.getElement().removeAttribute("sandbox");
         }
         else
         {
            frame_.getElement().setAttribute("sandbox", "allow-scripts");
         }
      }

      if (!useRawURL && !StringUtil.equals(URIConstants.ABOUT_BLANK, baseUrl_))
      {
         url = URIBuilder.fromUrl(url)
               .queryParam("tutorial_pane", "1")
               .get();
         
         frame_.setUrl(url);
      }
      else
      {
         frame_.setUrl(baseUrl_);
      }
   }
   
   
   
   
   private RStudioFrame frame_;
   private Toolbar toolbar_;
   private String baseUrl_;
   
   private static int popoutCount_ = 0;
   
   // Injected ----
   private final GlobalDisplay globalDisplay_;
   private final Commands commands_;

}
