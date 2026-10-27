package pe.upeu.agrosierra

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import kotlin.test.Test
import kotlin.test.assertEquals
import pe.upeu.agrosierra.db.AgroDb

class BaseEnMemoriaTest {
    @Test fun sqlDelightObservaDatosEnMemoria() {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        AgroDb.Schema.create(driver)
        val db = AgroDb(driver)
        db.agroDbQueries.upsertLote(1, "L-001", "Pampa", 12.5, "Papa", "Mantaro", 0L, 2.5)
        assertEquals(1, db.agroDbQueries.observarLotes("", "", 0L).executeAsList().size)
        driver.close()
    }
}
