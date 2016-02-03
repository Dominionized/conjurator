(ns conjurator.core
  (:require [play-clj.core :refer :all]
            [play-clj.ui :refer :all]
            [play-clj.g2d :refer :all]
            [play-clj.core :refer :all]
            [conjurator.utils :as u]))

(use 'clojure.pprint)

(declare update-accel update-physics reset-accel update-input fucking-print-player update-horizontal-player-speed update-vertical-player-speed apply-ground-resistance)

(defn- get-direction []
  (cond
    (key-pressed? :dpad-left) :left
    (key-pressed? :dpad-right) :right
    :else nil))

(defn- jumping-key-pressed? []
  (key-pressed? :x))

(defn- assoc-can-jump [entity]
  (assoc entity :can-jump? (= 0 (:y entity))))

(defn- move-player-x [entities]
  (if-let [direction (get-direction)]
    (map #(update-accel direction u/accel %) entities)
    entities))

(defn- update-accel [direction factor entity]
  (if (:player? entity)
    (case direction
      :left (assoc entity :x-accel (- factor))
      :right (assoc entity :x-accel factor)
      :up (assoc entity :y-accel factor)
      :down (assoc entity :y-accel (- factor))
      entity)
    entity))

(defn- update-jumping [entities]
  (if (and (jumping-key-pressed?)
           (:can-jump? (first (filter :player? entities))))
    (map #(update-accel :up u/jump-accel %) entities)
    entities))

(defn- TEMP-prevent-move [entity]
  (if (< (:y entity) 0) (assoc entity :y 0 :y-spd 0)
      entity))

(defn- reset-accel [entity]
  (assoc entity :x-accel 0 :y-accel 0))

(defn- update-physics [{player? :player? :as entity}]
  (if player?
    (let [{curr-x-spd :x-spd curr-y-spd :y-spd
           x-accel :x-accel y-accel :y-accel
           old-x :x old-y :y} entity]
      (-> entity
          (update-horizontal-player-speed x-accel)
          (update-vertical-player-speed y-accel)
          (update-vertical-player-speed u/gravity-accel)
          (apply-ground-resistance)
          (assoc :x (+ old-x curr-x-spd))
          (assoc :y (+ old-y curr-y-spd))
          (TEMP-prevent-move)
          (assoc-can-jump)
          (reset-accel)))
      entity))

(defn- update-horizontal-player-speed [{curr-x-spd :x-spd :as player} x-accel]
  (cond
    (> curr-x-spd u/max-player-speed) (assoc player :x-spd u/max-player-speed)
    (< curr-x-spd (- u/max-player-speed)) (assoc player :x-spd (- u/max-player-speed))
    :else (assoc player :x-spd (+ curr-x-spd x-accel))))

(defn- update-vertical-player-speed [{curr-y-spd :y-spd :as player} y-accel]
  (assoc player :y-spd (+ curr-y-spd y-accel)))

(defn- apply-ground-resistance [{:keys [x-spd] :as player}]
  (cond
    (pos? x-spd) (assoc player :x-spd (- x-spd u/ground-resistance))
    (neg? x-spd) (assoc player :x-spd (+ x-spd u/ground-resistance))
    :else player))

(defn- update-input [entities]
  (->> entities
        (move-player-x)
        (update-jumping)))

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
    (let [gab-ganon (assoc (texture "images/gabganon.png") :width 50 :height 50 :x 20 :y 20 :x-accel 0 :y-accel 0 :x-spd 0 :y-spd 0 :player? true :can-jump? true)
          background (assoc (texture "images/background.png") :width 800 :background? true)
          fps-counter (assoc (label "0" u/fps-counter-color) :fps? true)
          floor (assoc (texture "images/grass_block.png") :width 800 :height 20 :floor? true)]
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


