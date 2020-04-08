;; This Source Code Form is subject to the terms of the Mozilla Public
;; License, v. 2.0. If a copy of the MPL was not distributed with this
;; file, You can obtain one at http://mozilla.org/MPL/2.0/.
;;
;; This Source Code Form is "Incompatible With Secondary Licenses", as
;; defined by the Mozilla Public License, v. 2.0.
;;
;; Copyright (c) 2020 UXBOX Labs SL

(ns uxbox.main.ui.viewer.header
  (:require
   [beicon.core :as rx]
   [goog.events :as events]
   [goog.object :as gobj]
   [lentes.core :as l]
   [rumext.alpha :as mf]
   [uxbox.builtins.icons :as i]
   [uxbox.main.store :as st]
   [uxbox.main.ui.components.dropdown :refer [dropdown]]
   [uxbox.main.ui.workspace.header :refer [zoom-widget]]
   [uxbox.main.data.viewer :as dv]
   [uxbox.main.refs :as refs]
   [uxbox.util.data :refer [classnames]]
   [uxbox.util.dom :as dom]
   [uxbox.util.uuid :as uuid]
   [uxbox.util.i18n :as i18n :refer [t tr]]
   [uxbox.util.math :as mth]
   [uxbox.util.router :as rt])
  (:import goog.events.EventType
           goog.events.KeyCodes))


(mf/defc share-link
  [{:keys [page] :as props}]
  (let [show-dropdown? (mf/use-state false)
        dropdown-ref (mf/use-ref)
        token (:share-token page)

        create #(st/emit! dv/create-share-link)
        delete #(st/emit! dv/delete-share-link)
        href (.-href js/location)]
    [:*
     [:span.btn-primary.btn-small
      {:alt "Share link"
       :on-click #(swap! show-dropdown? not)}
      "Share link"]

     [:& dropdown {:show @show-dropdown?
                   :on-close #(swap! show-dropdown? not)
                   :container dropdown-ref}
      [:div.share-link-dropdown {:ref dropdown-ref}
       [:span.share-link-title "Share link"]
       [:div.share-link-input
        (if (string? token)
          [:span.link (str href "&token=" token)]
          [:span.link-placeholder "Share link will apear here"])
          [:span.link-button "Copy link"]]
       [:span.share-link-subtitle "Anyone with the link will have access"]
       [:div.share-link-buttons
        (if (string? token)
          [:button.btn-delete {:on-click delete} "Remove link"]
          [:button.btn-primary {:on-click create} "Create link"])]]]]))

(mf/defc header
  [{:keys [data index local fullscreen? toggle-fullscreen] :as props}]
  (let [{:keys [project file page frames]} data
        total (count frames)
        on-click #(st/emit! dv/toggle-thumbnails-panel)

        profile (mf/deref refs/profile)
        anonymous? (= uuid/zero (:id profile))

        project-id (get-in data [:project :id])
        file-id (get-in data [:file :id])
        page-id (get-in data [:page :id])

        on-edit #(st/emit! (rt/nav :workspace
                                   {:project-id project-id
                                    :file-id file-id}
                                   {:page-id page-id}))]
    [:header.viewer-header
     [:div.main-icon
      [:a {:on-click on-edit} i/logo-icon]]

     [:div.sitemap-zone {:alt (tr "header.sitemap")
                         :on-click on-click}
      [:span.project-name (:name project)]
      [:span "/"]
      [:span.file-name (:name file)]
      [:span "/"]
      [:span.page-name (:name page)]
      [:span.dropdown-button i/arrow-down]
      [:span.counters (str (inc index) " / " total)]]

     [:div.options-zone
      (when-not anonymous?
        [:& share-link {:page (:page data)}])
      (when-not anonymous?
        [:a {:on-click on-edit} "Edit page"])

      [:& zoom-widget
       {:zoom (:zoom local)
        :on-increase #(st/emit! dv/increase-zoom)
        :on-decrease #(st/emit! dv/decrease-zoom)
        :on-zoom-to-50 #(st/emit! dv/zoom-to-50)
        :on-zoom-to-100 #(st/emit! dv/reset-zoom)
        :on-zoom-to-200 #(st/emit! dv/zoom-to-200)}]

      [:span.btn-fullscreen.tooltip.tooltip-bottom
       {:alt "Full Screen"
        :on-click toggle-fullscreen}
       (if fullscreen?
         i/full-screen-off
         i/full-screen)]
      ]]))

