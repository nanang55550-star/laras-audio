package com.example.data

import android.content.Context
import com.example.R
import com.example.model.AudioFormat
import com.example.model.AudioTrack

object BundledCatalog {
    fun getSampleTracks(context: Context): List<AudioTrack> {
        val pkg = context.packageName
        return listOf(
            AudioTrack(
                id = "bundled_nocturne_emas",
                title = "Nocturne Emas",
                artist = "Kuartet Senyap",
                album = "Sesi Obsidian Vol. I",
                durationMs = 32_000L,
                uriString = "android.resource://$pkg/${R.raw.sample_nocturne_emas}",
                format = AudioFormat.FLAC,
                qualityLabel = "FLAC · 44.1 kHz",
                artworkResId = R.drawable.img_album_nocturne,
                isBundled = true,
                folderSource = "Bundled",
                dateAddedMillis = 0L,
                lyricsRaw = """
                    [ti:Nocturne Emas]
                    [ar:Kuartet Senyap]
                    [00:00.20]Lampu kota meredup perlahan di balik kaca
                    [00:04.20]Dentang tuts hangat menyapa ruang sunyi malam ini
                    [00:08.40]Di antara bayang obsidian dan cahaya keemasan
                    [00:12.60]Setiap nada mengalir tenang menenangkan pikiran
                    [00:16.80]Ketuk baris mana saja untuk melompat ke momen ini
                    [00:21.00]Harmoni jazz larut malam berputar tanpa tergesa
                    [00:25.20]Biarkan frekuensi hangat memeluk keheningan
                    [00:28.80]Hingga gema terakhir larut dalam gelap yang damai
                """.trimIndent()
            ),
            AudioTrack(
                id = "bundled_senja_obsidian",
                title = "Senja di Obsidian",
                artist = "Raka Pratama",
                album = "Akustik Tengah Malam",
                durationMs = 30_000L,
                uriString = "android.resource://$pkg/${R.raw.sample_senja_obsidian}",
                format = AudioFormat.MP3,
                qualityLabel = "MP3 · 320 kbps",
                artworkResId = R.drawable.img_album_senja,
                isBundled = true,
                folderSource = "Bundled",
                dateAddedMillis = 0L,
                lyricsRaw = """
                    [ti:Senja di Obsidian]
                    [ar:Raka Pratama]
                    [00:00.30]Ombak hitam memantulkan sisa cahaya senja
                    [00:04.00]Petikan dawai akustik menari di atas angin laut
                    [00:08.00]Langkah demi langkah waktu berjalan begitu lembut
                    [00:12.00]Menyimpan cerita hangat di ujung cakrawala
                    [00:16.00]Cobalah atur equalizer untuk rasa bass lebih dalam
                    [00:20.00]Atau nyalakan sleep timer sebelum terlelap nanti
                    [00:24.00]Garis emas tipis membelah cakrawala yang tenang
                    [00:27.40]Menutup hari dengan alunan melodi sederhana
                """.trimIndent()
            ),
            AudioTrack(
                id = "bundled_gema_harmoni",
                title = "Gema Harmoni",
                artist = "Studio Laras",
                album = "Resonansi Murni",
                durationMs = 28_000L,
                uriString = "android.resource://$pkg/${R.raw.sample_gema_harmoni}",
                format = AudioFormat.WAV,
                qualityLabel = "WAV · 16-bit PCM",
                artworkResId = R.drawable.img_album_gema,
                isBundled = true,
                folderSource = "Bundled",
                dateAddedMillis = 0L,
                lyricsRaw = """
                    [ti:Gema Harmoni]
                    [ar:Studio Laras]
                    [00:00.20]Gelombang suara murni terbentang tanpa kompresi
                    [00:03.80]Lapis demi lapis harmoni menyatu dalam ruang gelap
                    [00:07.60]Setiap getaran bernapas jernih dan seimbang
                    [00:11.40]Dengarkan detail halus di setiap rentang frekuensi
                    [00:15.20]Dari dentuman rendah hingga kilau nada tinggi
                    [00:19.00]Semuanya berpadu rapi dalam gema yang abadi
                    [00:22.80]Membawa ketenangan penuh sepanjang malam Anda
                    [00:25.80]Musik offline sejati, selalu hadir di genggaman
                """.trimIndent()
            )
        )
    }
}
