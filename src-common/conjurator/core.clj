(ns conjurator.core
  (:require [play-clj.core :refer :all]
            [play-clj.ui :refer :all]
            [play-clj.g2d :refer :all]
            [play-clj.core :refer :all]
            [conjurator.utils :as u]))

(use 'clojure.pprint)

(declare update-player-position update-physics reset-accel update-input fucking-print-player)

(defn- get-direction []
  (cond
    (key-pressed? :dpad-up) :up
    (key-pressed? :dpad-down) :down
    (key-pressed? :dpad-left) :left
    (key-pressed? :dpad-right) :right
    :else nil))

(defn- move-player [direction entities]
  (map #(update-player-position direction %) entities))

;;(move-player :right [{:player? true :x-accel 0}])

;;(update-player-position :right {:player? true})

(defn- update-player-position [direction entity]
  (if (:player? entity)
    (case direction
      :left (assoc entity :x-accel (- 0 u/accel))
      :right (assoc entity :x-accel u/accel)
      entity)
    entity))

(defn- reset-accel [entity]
  (assoc entity :x-accel 0))

(defn- update-physics [entity]
  (if (:player? entity)
      (-> entity
          (assoc :x-spd (+ (:x-spd entity) (:x-accel entity)))
          (assoc :x (+ (:x entity) (:x-spd entity)))
          (reset-accel))
      entity))

(defn- update-input [entities]
  (let [direction (get-direction)]
    (println direction)
    (cond
      direction (move-player direction entities)
      :else entities)))

(defn- update-fps-counter [entities]
  (map (fn [{fps? :fps? :as ent}]
         (if fps?
           (doto ent (label! set-text (str (game :fps))))
           ent))
       entities))

(defn- fucking-print-player [entities]
  (pprint (filter #(:player? %) entities))
  entities)

(defscreen main-screen
  :on-show
  (fn [screen entities]
    (update! screen :renderer (stage) :camera (orthographic))
    (let [gab-ganon (assoc (texture "images/gabganon.png") :x 20 :y 20 :x-accel 0 :x-spd 0 :player? true)
          background (assoc (texture "images/background.png") :width 800 :background? true)
          fps-counter (assoc (label "0" u/fps-counter-color) :fps? true)
          floor (assoc (texture "images/grass_block.png") :width 800 :floor? true)]
      [background floor gab-ganon fps-counter]))

  :on-resize
  (fn [screen entities]
    (height! screen 400)
    (width! screen 800))

  :on-render
  (fn [screen entities]
    (clear!)
    (->> entities
         (update-input)
         (fucking-print-player)
         (map update-physics)
         (fucking-print-player)
         (update-fps-counter)
         (render! screen))))

(defgame conjurator-game
  :on-create
  (fn [this]
    (music "music/YoshiTheme.mp3" :play)
    (set-screen! this main-screen)))

;;(app! :post-runnable #(set-screen! conjurator-game main-screen))


