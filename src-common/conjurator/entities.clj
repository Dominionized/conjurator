(ns conjurator.entities
  (:require [play-clj.core :refer :all]
            [play-clj.g2d :refer :all]
            [play-clj.ui :refer :all]
            [conjurator.utils :as u]))

(defn create-player []
  (assoc (texture "images/gabganon.png")
         :width 50 :height 50
         :x 20 :y 20
         :x-accel 0 :y-accel 0
         :x-spd 0 :y-spd 0
         :player? true
         :can-jump? true))

(defn create-background []
  (assoc (texture "images/background.png")
         :width 800
         :background? true))

(defn create-fps-counter []
  (assoc (label "0" u/fps-counter-color) :fps? true))

(defn create-floor []
  (assoc (texture "images/grass_block.png") :x 0 :y 0 :width 800 :height 20 :floor? true))

(defn create-tile []
  (assoc (texture "images/grass_block.png") :x 15 :y 15 :width 30 :height 30 :floor? true))
