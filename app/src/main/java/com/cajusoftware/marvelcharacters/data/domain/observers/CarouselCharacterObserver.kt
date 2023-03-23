package com.cajusoftware.marvelcharacters.data.domain.observers

import com.cajusoftware.marvelcharacters.data.domain.CarouselCharacter

interface CarouselCharacterObserver {
    fun collect(items: List<CarouselCharacter>)
}