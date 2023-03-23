package com.cajusoftware.marvelcharacters.data.domain

import com.cajusoftware.marvelcharacters.data.database.dtos.CharacterDto

sealed class CharacterModel {
    data class CarouselItem(val itemName: String = "carousel") : CharacterModel()
    data class CharacterItem(val character: CarouselCharacter) : CharacterModel()
    data class CharacterDtoItem(val character: CharacterDto) : CharacterModel()
    data class TitleItem(val title: String) : CharacterModel()
}
