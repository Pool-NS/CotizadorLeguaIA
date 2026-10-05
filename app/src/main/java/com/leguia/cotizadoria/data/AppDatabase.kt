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
        RegistroErrorEntity::class,
        ProformaEntity::class,
        TrabajoProduccionEntity::class,
        RequerimientoMaterialEntity::class,
        StockEntity::class,
        MovimientoStockEntity::class,
        NecesidadAbastecimientoEntity::class,
        PrediccionEntity::class
    ],
    version = 6,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun servicioDao(): ServicioDao
    abstract fun cotizacionDao(): CotizacionDao
    abstract fun trazabilidadDao(): TrazabilidadDao
    abstract fun parametroPrecioDao(): ParametroPrecioDao
    abstract fun registroErrorDao(): RegistroErrorDao
    abstract fun proformaDao(): ProformaDao
    abstract fun trabajoProduccionDao(): TrabajoProduccionDao
    abstract fun inventarioDao(): InventarioDao
    abstract fun prediccionDao(): PrediccionDao

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

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""CREATE TABLE IF NOT EXISTS `proforma` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `cotizacionId` INTEGER NOT NULL, `codigo` TEXT NOT NULL, `fechaCreacion` INTEGER NOT NULL, `responsable` TEXT NOT NULL, `observacion` TEXT, FOREIGN KEY(`cotizacionId`) REFERENCES `cotizacion`(`id`) ON UPDATE NO ACTION ON DELETE RESTRICT)""")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_proforma_cotizacionId` ON `proforma` (`cotizacionId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_proforma_fechaCreacion` ON `proforma` (`fechaCreacion`)")
                db.execSQL("""CREATE TABLE IF NOT EXISTS `trabajo_produccion` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `cotizacionId` INTEGER NOT NULL, `servicioId` INTEGER NOT NULL, `fechaPrevista` INTEGER NOT NULL, `prioridad` TEXT NOT NULL, `estado` TEXT NOT NULL, `responsable` TEXT NOT NULL, `observaciones` TEXT, FOREIGN KEY(`cotizacionId`) REFERENCES `cotizacion`(`id`) ON UPDATE NO ACTION ON DELETE RESTRICT, FOREIGN KEY(`servicioId`) REFERENCES `servicio`(`id`) ON UPDATE NO ACTION ON DELETE RESTRICT)""")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_trabajo_produccion_cotizacionId` ON `trabajo_produccion` (`cotizacionId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_trabajo_produccion_servicioId` ON `trabajo_produccion` (`servicioId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_trabajo_produccion_fechaPrevista` ON `trabajo_produccion` (`fechaPrevista`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_trabajo_produccion_estado` ON `trabajo_produccion` (`estado`)")
                db.execSQL("""CREATE TABLE IF NOT EXISTS `requerimiento_material` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `trabajoId` INTEGER NOT NULL, `materialId` INTEGER NOT NULL, `cantidad` REAL NOT NULL, `unidad` TEXT NOT NULL, `origen` TEXT NOT NULL, FOREIGN KEY(`trabajoId`) REFERENCES `trabajo_produccion`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE, FOREIGN KEY(`materialId`) REFERENCES `material`(`id`) ON UPDATE NO ACTION ON DELETE RESTRICT)""")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_requerimiento_material_trabajoId` ON `requerimiento_material` (`trabajoId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_requerimiento_material_materialId` ON `requerimiento_material` (`materialId`)")
                db.execSQL("""CREATE TABLE IF NOT EXISTS `stock` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `materialId` INTEGER NOT NULL, `cantidadDisponible` REAL NOT NULL, `unidad` TEXT NOT NULL, `origen` TEXT NOT NULL, `actualizadoEn` INTEGER NOT NULL, FOREIGN KEY(`materialId`) REFERENCES `material`(`id`) ON UPDATE NO ACTION ON DELETE RESTRICT)""")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_stock_materialId` ON `stock` (`materialId`)")
                db.execSQL("""CREATE TABLE IF NOT EXISTS `movimiento_stock` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `materialId` INTEGER NOT NULL, `cantidadCambio` REAL NOT NULL, `tipo` TEXT NOT NULL, `fecha` INTEGER NOT NULL, `responsable` TEXT NOT NULL, `observacion` TEXT, `origen` TEXT NOT NULL, FOREIGN KEY(`materialId`) REFERENCES `material`(`id`) ON UPDATE NO ACTION ON DELETE RESTRICT)""")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_movimiento_stock_materialId` ON `movimiento_stock` (`materialId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_movimiento_stock_fecha` ON `movimiento_stock` (`fecha`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_movimiento_stock_origen` ON `movimiento_stock` (`origen`)")
                db.execSQL("""CREATE TABLE IF NOT EXISTS `necesidad_abastecimiento` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `materialId` INTEGER NOT NULL, `trabajoId` INTEGER, `cantidadNecesaria` REAL NOT NULL, `unidad` TEXT NOT NULL, `estado` TEXT NOT NULL, `creadoEn` INTEGER NOT NULL, `responsable` TEXT NOT NULL, FOREIGN KEY(`materialId`) REFERENCES `material`(`id`) ON UPDATE NO ACTION ON DELETE RESTRICT, FOREIGN KEY(`trabajoId`) REFERENCES `trabajo_produccion`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL)""")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_necesidad_abastecimiento_materialId` ON `necesidad_abastecimiento` (`materialId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_necesidad_abastecimiento_trabajoId` ON `necesidad_abastecimiento` (`trabajoId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_necesidad_abastecimiento_estado` ON `necesidad_abastecimiento` (`estado`)")
                db.execSQL("""CREATE TABLE IF NOT EXISTS `prediccion` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `cotizacionId` INTEGER NOT NULL, `probabilidadConversion` REAL NOT NULL, `claseEstimada` TEXT NOT NULL, `fechaPrediccion` INTEGER NOT NULL, `versionModelo` TEXT NOT NULL, `esSintetica` INTEGER NOT NULL, FOREIGN KEY(`cotizacionId`) REFERENCES `cotizacion`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE)""")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_prediccion_cotizacionId` ON `prediccion` (`cotizacionId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_prediccion_fechaPrediccion` ON `prediccion` (`fechaPrediccion`)")
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `cotizacion` ADD COLUMN `tipoMoto` TEXT")
                db.execSQL("ALTER TABLE `cotizacion` ADD COLUMN `tipoCarpa` TEXT")
                db.execSQL("ALTER TABLE `cotizacion` ADD COLUMN `cantidadVentanas` INTEGER")
                db.execSQL("ALTER TABLE `cotizacion` ADD COLUMN `cantidadPuertas` INTEGER")
                db.execSQL("UPDATE `servicio` SET `nombre` = 'Tapizado de moto' WHERE lower(`nombre`) = 'tapizado'")
                db.execSQL("UPDATE `parametro_precio` SET `concepto` = 'MEDIDA:BASE:' || substr(`concepto`, 8) WHERE `concepto` LIKE 'MEDIDA:%' AND instr(substr(`concepto`, 8), ':') = 0")
            }
        }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `cotizacion` ADD COLUMN `materialDescripcion` TEXT")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "cotizador_leguia_db"
                )
                    .addMigrations(MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6)
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Inserción directa en la tabla de Room al crear la Base de Datos
                            db.execSQL("INSERT INTO servicio (id, nombre, descripcion) VALUES (1, 'Carpas', 'Servicio configurable; validar características con Leguía')")
                            db.execSQL("INSERT INTO servicio (id, nombre, descripcion) VALUES (2, 'Toldos', 'Servicio configurable; validar características con Leguía')")
                            db.execSQL("INSERT INTO servicio (id, nombre, descripcion) VALUES (3, 'Tapizado de moto', 'Cotización de tapizado para motocicletas')")
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
