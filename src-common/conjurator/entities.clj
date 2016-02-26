(ns conjurator.entities
  (:require [play-clj.core :refer :all]
            [play-clj.g2d :refer :all]
            [play-clj.ui :refer :all]
            [conjurator.utils :as u]))

(defn get-texture [tex]
  (case tex
    :grass (texture "images/grass_block.png")
    :gabganon (texture "images/gabganon.png")
    :background (texture "images/background.png")))

(defn create-player []
  (assoc (get-texture :gabganon)
         :width 50 :height 50
         :x 20 :y 20
         :x-accel 0 :y-accel 0
         :x-spd 0 :y-spd 0
         :player? true
         :can-jump? true))

(defn create-background []
  (assoc (get-texture :background)
         :width 800
         :background? true))

(defn create-fps-counter []
  (assoc (label "0" u/fps-counter-color) :fps? true))

(defn create-floor []
  (assoc (get-texture :grass) :x 0 :y 0 :width 800 :height 20 :floor? true))

(defn create-tiles [& coords]
  (for [[x y] coords]
    (assoc (get-texture :grass) :x x :y y :width 40 :height 40 :floor? true)))

(defn texture-at [tex x y random]
  (let [size (if random (rand 40) 40)]
    (assoc (get-texture tex)
          :x x :y y
          :width size :height size)))

(defn grass-at [x y]
  (assoc (texture-at :grass x y false) :floor? true))

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

(defn get-colliding-entity [{:keys [x y width height] :as player} entities]
  (if-let [coll-ent-center (in-entity? (+ x (/ width 2)) y entities)]
    coll-ent-center
    (if-let [coll-ent-left (in-entity? x y entities)]
      coll-ent-left
      (when-let [coll-ent-right (in-entity? (+ x width) y entities)]
        coll-ent-right))))
