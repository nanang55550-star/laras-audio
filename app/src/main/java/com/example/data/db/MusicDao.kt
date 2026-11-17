package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MusicDao {
    // Playlists
    @Query("SELECT * FROM playlists ORDER BY createdAt DESC")
    fun observePlaylists(): Flow<List<PlaylistEntity>>

    @Query("SELECT * FROM playlist_tracks ORDER BY position ASC, addedAt ASC")
    fun observeAllPlaylistTracks(): Flow<List<PlaylistTrackEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: PlaylistEntity): Long

    @Query("UPDATE playlists SET name = :newName, description = :newDescription WHERE id = :playlistId")
    suspend fun updatePlaylist(playlistId: Long, newName: String, newDescription: String)

    @Query("DELETE FROM playlists WHERE id = :playlistId")
    suspend fun deletePlaylist(playlistId: Long)

    @Query("SELECT COALESCE(MAX(position), -1) + 1 FROM playlist_tracks WHERE playlistId = :playlistId")
    suspend fun getNextTrackPosition(playlistId: Long): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addTrackToPlaylist(crossRef: PlaylistTrackEntity)

    @Query("DELETE FROM playlist_tracks WHERE playlistId = :playlistId AND trackId = :trackId")
    suspend fun removeTrackFromPlaylist(playlistId: Long, trackId: String)

    // Favorites
    @Query("SELECT trackId FROM favorite_tracks")
    fun observeFavoriteIds(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavorite(entity: FavoriteTrackEntity)

    @Query("DELETE FROM favorite_tracks WHERE trackId = :trackId")
    suspend fun removeFavorite(trackId: String)

    // Custom Equalizer Presets
    @Query("SELECT * FROM custom_eq_presets ORDER BY createdAt DESC")
    fun observeCustomEqPresets(): Flow<List<CustomEqPresetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomEqPreset(preset: CustomEqPresetEntity): Long

    @Query("DELETE FROM custom_eq_presets WHERE id = :id")
    suspend fun deleteCustomEqPreset(id: Long)

    // Custom / Imported LRC Lyrics
    @Query("SELECT * FROM custom_lyrics")
    fun observeCustomLyrics(): Flow<List<CustomLyricEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCustomLyric(entity: CustomLyricEntity)

    // Imported SAF Audio Tracks
    @Query("SELECT * FROM imported_tracks ORDER BY addedAt DESC")
    fun observeImportedTracks(): Flow<List<ImportedTrackEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertImportedTrack(entity: ImportedTrackEntity)

    @Query("DELETE FROM imported_tracks WHERE id = :id")
    suspend fun deleteImportedTrack(id: String)
}
