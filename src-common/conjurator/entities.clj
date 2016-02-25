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

(defn create-tiles []
  (let [grass (texture "images/grass_block.png")]
    [(assoc grass :x 15 :y 15 :width 30 :height 30 :floor? true)
     (assoc grass :x 200 :y 30 :width 40 :height 40 :floor? true)]))

(defn in-entity? [x y entities]
  (->> entities
       (filter :floor?)
       (filter (fn [ent]
                 (let [x1 (:x ent)
                       y1 (:y ent)
                       x2 (+ x1 (:width ent))
                       y2 (+ y1 (:height ent))]
                   (and (> x x1)
                        (> y y1)
                        (< x x2)
                        (< y y2)))))
       first))
