package com.leguia.cotizadoria.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        ServicioEntity::class,
        MaterialEntity::class,
        ParametroPrecioEntity::class,
        CotizacionEntity::class,
        EventoTrazabilidadEntity::class,
        RegistroErrorEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun servicioDao(): ServicioDao
    abstract fun cotizacionDao(): CotizacionDao
    abstract fun trazabilidadDao(): TrazabilidadDao
    abstract fun parametroPrecioDao(): ParametroPrecioDao
    abstract fun registroErrorDao(): RegistroErrorDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 1. Tabla parametro_precio
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `parametro_precio` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `servicioId` INTEGER NOT NULL,
                        `concepto` TEXT NOT NULL,
                        `precioUnitario` REAL NOT NULL,
                        `unidadMedida` TEXT NOT NULL,
                        `activo` INTEGER NOT NULL,
                        FOREIGN KEY(`servicioId`) REFERENCES `servicio`(`id`) ON DELETE CASCADE
                    )
                """)
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_parametro_precio_servicioId` ON `parametro_precio` (`servicioId`)")

                // 2. Tabla cotizacion
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `cotizacion` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `codigoCotizacion` TEXT NOT NULL,
                        `servicioId` INTEGER NOT NULL,
                        `materialId` INTEGER,
                        `operador` TEXT NOT NULL,
                        `requerimientoCliente` TEXT NOT NULL,
                        `interpretacionIaJson` TEXT,
                        `corregidoPorHumano` INTEGER NOT NULL,
                        `medidaLargo` REAL,
                        `medidaAncho` REAL,
                        `medidaAlto` REAL,
                        `precioReferencial` REAL NOT NULL,
                        `descuentoMonto` REAL NOT NULL,
                        `descuentoPorcentaje` REAL NOT NULL,
                        `precioFinal` REAL NOT NULL,
                        `estado` TEXT NOT NULL,
                        `fechaHoraInicio` INTEGER NOT NULL,
                        `fechaHoraFin` INTEGER,
                        `esModoOffline` INTEGER NOT NULL,
                        FOREIGN KEY(`servicioId`) REFERENCES `servicio`(`id`) ON DELETE RESTRICT,
                        FOREIGN KEY(`materialId`) REFERENCES `material`(`id`) ON DELETE SET NULL
                    )
                """)
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_cotizacion_codigoCotizacion` ON `cotizacion` (`codigoCotizacion`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_cotizacion_servicioId` ON `cotizacion` (`servicioId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_cotizacion_materialId` ON `cotizacion` (`materialId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_cotizacion_estado` ON `cotizacion` (`estado`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_cotizacion_fechaHoraInicio` ON `cotizacion` (`fechaHoraInicio`)")

                // 3. Tabla evento_trazabilidad
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `evento_trazabilidad` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `cotizacionId` INTEGER,
                        `tipoEvento` TEXT NOT NULL,
                        `descripcion` TEXT NOT NULL,
                        `timestamp` INTEGER NOT NULL,
                        `operador` TEXT NOT NULL,
                        FOREIGN KEY(`cotizacionId`) REFERENCES `cotizacion`(`id`) ON DELETE CASCADE
                    )
                """)
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_evento_trazabilidad_cotizacionId` ON `evento_trazabilidad` (`cotizacionId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_evento_trazabilidad_timestamp` ON `evento_trazabilidad` (`timestamp`)")

                // 4. Tabla registro_error
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `registro_error` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `cotizacionId` INTEGER NOT NULL,
                        `codigoError` TEXT NOT NULL,
                        `descripcion` TEXT NOT NULL,
                        `atribuibleASistema` INTEGER NOT NULL,
                        `timestamp` INTEGER NOT NULL,
                        FOREIGN KEY(`cotizacionId`) REFERENCES `cotizacion`(`id`) ON DELETE CASCADE
                    )
                """)
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_registro_error_cotizacionId` ON `registro_error` (`cotizacionId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_registro_error_codigoError` ON `registro_error` (`codigoError`)")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "cotizador_leguia_db"
                )
                    .addMigrations(MIGRATION_2_3)
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Inserción directa en la tabla de Room al crear la Base de Datos
                            db.execSQL("INSERT INTO servicio (id, nombre) VALUES (1, 'Corte y Confección')")
                            db.execSQL("INSERT INTO servicio (id, nombre) VALUES (2, 'Bordado Personalizado')")
                            db.execSQL("INSERT INTO servicio (id, nombre) VALUES (3, 'Estampado Textil')")
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}