package com.fisma.trinity

import com.fisma.trinity.util.DynamicGrid
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@Config(manifest = Config.NONE, sdk = [16])
@RunWith(RobolectricTestRunner::class)
class DynamicGridTest {
  var grid: DynamicGrid? = null
  var columnCount = 4
  var rowCount = 6

  val mScreenWidth = 720
  val mScreenHeight = 1440

  @Before
  fun setUp() {
    DynamicGrid.mScreenHeight = mScreenHeight
    DynamicGrid.mScreenWidth = mScreenWidth

    val cell = DynamicGrid.CellProfile(null)
    val rowProfile = DynamicGrid.GridOrientationSpec(rowCount, rowCount)
    val columnProfile = DynamicGrid.GridOrientationSpec(columnCount, columnCount)

    val bottomMargin = 268
    val topMargin = 48
    val margin = DynamicGrid.Margin(0, 0, topMargin, bottomMargin)

    val params = DynamicGrid.DynamicGridParams()
    params.columnProfile = columnProfile
    params.rowProfile = rowProfile
    params.height = -1
    params.width = -1

    grid = DynamicGrid(null, params, cell)
  }

  @Test
  fun shouldHaveTheCorrectHeight() {
    var expectedHeight = mScreenHeight
    var expectedCellHeight = expectedHeight / rowCount

    assertNotNull(grid)
    assertEquals(expectedHeight, grid!!.getParams().height)
    assertEquals(expectedCellHeight, grid!!.cellHeight)

    expectedHeight -= 132
    expectedCellHeight = expectedHeight / rowCount
    grid!!.setMargin(DynamicGrid.Margin(0, 0, 48, 84))
    assertEquals(expectedHeight, grid!!.getParams().height)
    assertEquals(expectedCellHeight, grid!!.cellHeight)

    expectedHeight -= 184
    expectedCellHeight = expectedHeight / rowCount
    grid!!.setMargin(DynamicGrid.Margin(0, 0, 48, 268))
    assertEquals(expectedHeight, grid!!.getParams().height)
    assertEquals(expectedCellHeight, grid!!.cellHeight)
  }

  @Test
  fun shouldHaveTheCorrectWidth() {
    var expectedWidth = mScreenWidth
    var expectedCellWidth = expectedWidth / columnCount

    assertNotNull(grid)
    assertEquals(expectedWidth, grid!!.getParams().width)
    assertEquals(expectedCellWidth, grid!!.cellWidth)

    expectedWidth -= 50
    expectedCellWidth = expectedWidth / columnCount

    grid!!.setMargin(DynamicGrid.Margin(25, 25, 0, 0))
    assertEquals(expectedWidth, grid!!.getParams().width)
    assertEquals(expectedCellWidth, grid!!.cellWidth)
  }

  @Test
  fun shouldHaveTheCorrectHeightWithStatusBar() {
    val statusBarHeight = 48
    var expectedHeight = mScreenHeight - statusBarHeight
    var expectedCellHeight = expectedHeight / rowCount

    DynamicGrid.statusBarHeight = statusBarHeight
    DynamicGrid.navBarHeight = 0
    assertNotNull(grid)
    assertEquals(expectedHeight, grid!!.getParams().height)
    assertEquals(expectedCellHeight, grid!!.cellHeight)

    expectedHeight = expectedHeight - 50
    expectedCellHeight = expectedHeight / rowCount
    grid!!.setMargin(DynamicGrid.Margin(0, 0, 25, 25))
    assertEquals(expectedHeight, grid!!.getParams().height)
    assertEquals(expectedCellHeight, grid!!.cellHeight)
  }

  @Test
  fun shouldHaveTheCorrectHeightWithNavBar() {
    val navBarHeight = 84
    var expectedHeight = mScreenHeight - navBarHeight
    var expectedCellHeight = expectedHeight / rowCount

    DynamicGrid.navBarHeight = navBarHeight
    DynamicGrid.statusBarHeight = 0
    assertNotNull(grid)
    assertEquals(expectedHeight, grid!!.getParams().height)
    assertEquals(expectedCellHeight, grid!!.cellHeight)

    expectedHeight = expectedHeight - 50
    expectedCellHeight = expectedHeight / rowCount
    grid!!.setMargin(DynamicGrid.Margin(0, 0, 25, 25))
    assertEquals(expectedHeight, grid!!.getParams().height)
    assertEquals(expectedCellHeight, grid!!.cellHeight)
  }
}
