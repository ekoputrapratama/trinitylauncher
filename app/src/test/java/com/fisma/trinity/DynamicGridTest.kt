package com.fisma.trinity


import com.fisma.trinity.util.DynamicGrid
import junit.framework.Assert.*
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@Config(manifest = Config.NONE, sdk = [19])
@RunWith(RobolectricTestRunner::class)
class DynamicGridTest {
  var grid: DynamicGrid? = null
  var columnCount = 4
  var rowCount = 6

  val mScreenWidth = 720
  val mScreenHeight = 1440

  @After
  fun afterEach() {
    grid = null
  }

  @Test
  fun shouldCreateANewInstance() {
    grid = DynamicGrid()

    assertNotNull(grid)
    assertNotNull(grid!!.getGridParams())
    assertNotNull(grid!!.getGridParams().margin)
    assertNotNull(grid!!.getGridParams().columnProfile)
    assertNotNull(grid!!.getGridParams().rowProfile)

    val columnSpec = DynamicGrid.GridOrientationSpec(columnCount, columnCount)
    val rowSpec = DynamicGrid.GridOrientationSpec(rowCount, rowCount)
    val cell = DynamicGrid.CellParams()
    val params = DynamicGrid.GridParams().apply {
      setColumnSpec(columnSpec)
      setRowSpec(rowSpec)
    }
    grid = DynamicGrid(params, cell)

    assertNotNull(grid)
    assertNotNull(grid!!.getGridParams())
    assertEquals(params, grid!!.getGridParams())
    assertEquals(cell, grid!!.getCellParams())
    assertNotNull(grid!!.getGridParams().rowProfile)
    assertEquals(rowSpec, grid!!.getGridParams().rowProfile)
    assertNotNull(grid!!.getGridParams().columnProfile)
    assertEquals(columnSpec, grid!!.getGridParams().columnProfile)
  }

  @Test
  fun shouldCloneInstance() {
    val grid1 = DynamicGrid()

    assertNotNull(grid1)
    assertNotNull(grid1.getName())
    assertEquals("DynamicGrid", grid1.getName())
    assertNotNull(grid1.getGridParams())
    assertNotNull(grid1.getGridParams().margin)
    assertNotNull(grid1.getGridParams().columnProfile)
    assertNotNull(grid1.getGridParams().rowProfile)

    val grid2 = grid1.clone("TestGrid")
    assertNotNull(grid2)
    assertNotNull(grid2.getName())
    assertEquals("TestGrid", grid2.getName())
    assertNotSame(grid2.getName(), grid1.getName())

    val margin = DynamicGrid.Margin(0, 0, 25, 25)
    grid2.setMargin(margin)
    assertEquals(margin, grid2.getGridParams().margin)
    assertNotSame(margin, grid1.getGridParams().margin)

    val columnSpec = DynamicGrid.GridOrientationSpec(columnCount, columnCount)
    val rowSpec = DynamicGrid.GridOrientationSpec(rowCount, rowCount)

    grid2.getGridParams().setColumnSpec(columnSpec)
    assertNotNull(grid2.getGridParams().columnProfile)
    assertEquals(columnSpec, grid2.getGridParams().columnProfile)
    assertNotSame(columnSpec, grid1.getGridParams().columnProfile)

    grid2.getGridParams().setRowSpec(rowSpec)
    assertNotNull(grid2.getGridParams().rowProfile)
    assertEquals(rowSpec, grid2.getGridParams().rowProfile)
    assertNotSame(rowSpec, grid1.getGridParams().rowProfile)
  }

  @Test
  fun shouldHaveTheCorrectHeight() {
    grid = setupDefaultGrid()
    var expectedHeight = mScreenHeight
    var expectedCellHeight = expectedHeight

    assertNotNull(grid)
    assertEquals(expectedHeight, grid!!.getGridParams().height)

    var cellParams = grid!!.getCellParams()
    assertEquals(expectedCellHeight, cellParams.height)

    expectedHeight -= 132
    expectedCellHeight = expectedHeight / rowCount
    grid!!.setMargin(DynamicGrid.Margin(0, 0, 48, 84))
    assertEquals(expectedHeight, grid!!.getGridParams().height)

    // change the row spec
    grid!!.getGridParams().setRowSpec(DynamicGrid.GridOrientationSpec(rowCount, rowCount))
    cellParams = grid!!.getCellParams()
    assertEquals(expectedCellHeight, cellParams.height)

    expectedHeight -= 184
    expectedCellHeight = expectedHeight / rowCount
    grid!!.setMargin(DynamicGrid.Margin(0, 0, 48, 268))
    assertEquals(expectedHeight, grid!!.getGridParams().height)
    cellParams = grid!!.getCellParams()
    assertEquals(expectedCellHeight, cellParams.height)
  }

  @Test
  fun shouldHaveTheCorrectWidth() {
    grid = setupDefaultGrid()
    var expectedWidth = mScreenWidth
    var expectedCellWidth = expectedWidth

    assertNotNull(grid)
    assertEquals(expectedWidth, grid!!.getGridParams().width)
    var cellParams = grid!!.getCellParams()
    assertEquals(expectedCellWidth, cellParams.width)

    expectedWidth -= 50
    expectedCellWidth = expectedWidth / columnCount

    grid!!.setMargin(DynamicGrid.Margin(25, 25, 0, 0))
    assertEquals(expectedWidth, grid!!.getGridParams().width)

    // change the column spec
    grid!!.getGridParams().setColumnSpec(DynamicGrid.GridOrientationSpec(columnCount, columnCount))
    cellParams = grid!!.getCellParams()
    assertEquals(expectedCellWidth, cellParams.width)
  }

  @Test
  fun shouldHaveTheCorrectHeightWithStatusBar() {
    grid = setupDefaultGrid()
    val statusBarHeight = 48
    var expectedHeight = mScreenHeight - statusBarHeight
    var expectedCellHeight = expectedHeight

    DynamicGrid.statusBarHeight = statusBarHeight
    DynamicGrid.navBarHeight = 0
    assertNotNull(grid)
    assertEquals(expectedHeight, grid!!.getGridParams().height)
    var cellParams = grid!!.getCellParams()
    assertEquals(expectedCellHeight, cellParams.height)

    expectedHeight -= 50
    expectedCellHeight = expectedHeight / rowCount
    grid!!.setMargin(DynamicGrid.Margin(0, 0, 25, 25))
    assertEquals(expectedHeight, grid!!.getGridParams().height)

    // change the row spec
    grid!!.getGridParams().setRowSpec(DynamicGrid.GridOrientationSpec(rowCount, rowCount))
    cellParams = grid!!.getCellParams()
    assertEquals(expectedCellHeight, cellParams.height)
  }

  @Test
  fun shouldHaveTheCorrectHeightWithNavBar() {
    grid = setupDefaultGrid()
    val navBarHeight = 84
    var expectedHeight = mScreenHeight - navBarHeight
    var expectedCellHeight = expectedHeight

    DynamicGrid.navBarHeight = navBarHeight
    DynamicGrid.statusBarHeight = 0
    assertNotNull(grid)
    assertEquals(expectedHeight, grid!!.getGridParams().height)
    var cellParams = grid!!.getCellParams()
    assertEquals(expectedCellHeight, cellParams.height)

    expectedHeight -= 50
    expectedCellHeight = expectedHeight / rowCount
    grid!!.setMargin(DynamicGrid.Margin(0, 0, 25, 25))
    assertEquals(expectedHeight, grid!!.getGridParams().height)

    // change the row spec
    grid!!.getGridParams().setRowSpec(DynamicGrid.GridOrientationSpec(rowCount, rowCount))
    cellParams = grid!!.getCellParams()
    assertEquals(expectedCellHeight, cellParams.height)
  }

  fun setupDefaultGrid(): DynamicGrid {
    val cell = DynamicGrid.CellParams()
    val params = DynamicGrid.GridParams()
    DynamicGrid.mScreenWidth = mScreenWidth
    DynamicGrid.mScreenHeight = mScreenHeight

    return DynamicGrid(params, cell)
  }

  fun setupGridWithSize(width: Int, height: Int): DynamicGrid {
    val cell = DynamicGrid.CellParams()
    val params = DynamicGrid.GridParams()
    params.width = width
    params.height = height

    return DynamicGrid(params, cell)
  }

  fun setupGridWithColumnAndRow(rowSpec: DynamicGrid.GridOrientationSpec, columnSpec: DynamicGrid.GridOrientationSpec): DynamicGrid {
    val cell = DynamicGrid.CellParams()
    val params = DynamicGrid.GridParams().apply {
      setColumnSpec(columnSpec)
      setRowSpec(rowSpec)
    }

    return DynamicGrid(params, cell)
  }

  fun setupGridWithItemSize(width: Int, height: Int) {

  }

  fun getDefaultRowSpec(): DynamicGrid.GridOrientationSpec {
    return DynamicGrid.GridOrientationSpec(rowCount, rowCount)
  }

  fun getDefaultColumnSpec(): DynamicGrid.GridOrientationSpec {
    return DynamicGrid.GridOrientationSpec(columnCount, columnCount)
  }
}
