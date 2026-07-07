import { useEffect, useState } from 'react'
import { getAllStocks } from '../../services/api'
import { Activity, TrendingUp, TrendingDown, ChevronRight } from 'lucide-react'
import StockDetailModal from './StockDetailModal'

function StockList({ liveStocks, globalSearch }) {
  const [stocks, setStocks] = useState([])
  const [loading, setLoading] = useState(true)
  const [selectedStock, setSelectedStock] = useState(null)

  useEffect(() => {
    fetchStocks()
    const interval = setInterval(fetchStocks, 10000)
    return () => clearInterval(interval)
  }, [])

  // Merge live WebSocket updates with fetched stocks
  useEffect(() => {
    if (Object.keys(liveStocks).length > 0) {
      setStocks(prev => prev.map(stock => {
        const live = liveStocks[stock.symbol]
        return live ? { ...stock, ...live } : stock
      }))
    }
  }, [liveStocks])

  // Keep the selected stock updated with latest live data too
  useEffect(() => {
    if (selectedStock) {
      setSelectedStock(prev => {
        const live = liveStocks[prev.symbol]
        return live ? { ...prev, ...live } : prev
      })
    }
  }, [liveStocks])

  const fetchStocks = async () => {
    try {
      const response = await getAllStocks()
      setStocks(response.data)
    } catch (error) {
      console.error('Error fetching stocks:', error)
    } finally {
      setLoading(false)
    }
  }

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="text-gray-500 font-mono text-sm uppercase tracking-widest animate-pulse">
          Loading Market Data...
        </div>
      </div>
    )
  }

  const filteredStocks = stocks.filter(stock => 
    !globalSearch || stock.symbol.toLowerCase().includes(globalSearch.toLowerCase())
  )

  return (
    <>
      <div>
        <div className="flex items-center justify-between mb-4">
          <div className="flex items-center gap-4">
            <h2 className="text-gray-100 text-xl font-bold tracking-tight">
              Market Overview
            </h2>
            <div className="h-4 w-px bg-gray-800"></div>
            <span className="text-gray-500 text-sm font-mono">
              {filteredStocks.length} Assets
            </span>
          </div>
        </div>

        {stocks.length === 0 ? (
          <div className="bg-[#111827] border border-gray-800/60 rounded-2xl p-16 text-center shadow-lg">
            <Activity className="text-gray-600 mx-auto mb-4" size={48} />
            <p className="text-gray-400 font-medium text-lg">
              Waiting for live data from Finnhub...
            </p>
            <p className="text-gray-600 text-sm mt-2">
              Ensure your backend is running and Finnhub API key is set.
            </p>
          </div>
        ) : (
          <div className="bg-[#0a0a0a] border border-gray-800/60 rounded-xl overflow-hidden shadow-2xl">
            <div className="overflow-x-auto">
              <table className="w-full whitespace-nowrap">
                <thead>
                  <tr className="border-b border-gray-800/60 bg-[#111827]/50">
                    <th className="text-left text-gray-500 text-[10px] font-bold uppercase tracking-wider px-6 py-4">Symbol</th>
                    <th className="text-right text-gray-500 text-[10px] font-bold uppercase tracking-wider px-6 py-4">Last Price</th>
                    <th className="text-right text-gray-500 text-[10px] font-bold uppercase tracking-wider px-6 py-4">Change</th>
                    <th className="text-right text-gray-500 text-[10px] font-bold uppercase tracking-wider px-6 py-4">Volume</th>
                    <th className="text-right text-gray-500 text-[10px] font-bold uppercase tracking-wider px-6 py-4">RSI (14)</th>
                    <th className="text-right text-gray-500 text-[10px] font-bold uppercase tracking-wider px-6 py-4">P/E</th>
                    <th className="w-12"></th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-800/30">
                  {filteredStocks.length === 0 ? (
                    <tr>
                      <td colSpan={7} className="text-center text-gray-500 py-12 font-mono text-sm">
                        No assets found for "{globalSearch}"
                      </td>
                    </tr>
                  ) : filteredStocks.map((stock) => {
                    const isPositive = (stock.priceChange ?? 0) >= 0
                    const isSelected = selectedStock?.symbol === stock.symbol
                    return (
                      <tr
                        key={stock.symbol}
                        onClick={() => setSelectedStock(stock)}
                        className={`transition-all cursor-pointer group ${
                          isSelected
                            ? 'bg-green-500/10'
                            : liveStocks[stock.symbol]
                            ? 'bg-green-500/5 hover:bg-gray-800/40'
                            : 'hover:bg-gray-800/40'
                        }`}
                      >
                        <td className="px-6 py-4">
                          <div className="flex items-center gap-3">
                            <div
                              className="w-8 h-8 rounded-lg flex items-center justify-center text-xs font-bold shadow-inner"
                              style={{
                                background: isPositive ? '#064e3b' : '#450a0a',
                                color: isPositive ? '#34d399' : '#f87171',
                                border: `1px solid ${isPositive ? '#065f46' : '#7f1d1d'}`
                              }}
                            >
                              {stock.symbol.slice(0, 2)}
                            </div>
                            <span className="text-gray-200 font-bold text-sm tracking-wide group-hover:text-white transition-colors">
                              {stock.symbol}
                            </span>
                          </div>
                        </td>
                        <td className="px-6 py-4 text-right">
                          <span className="text-white text-sm font-mono font-medium">
                            ${stock.currentPrice?.toFixed(2) ?? '--'}
                          </span>
                        </td>
                        <td className="px-6 py-4 text-right">
                          <div className={`inline-flex items-center justify-end gap-1.5 px-2.5 py-1 rounded-md text-xs font-mono font-semibold ${
                            isPositive ? 'bg-green-500/10 text-green-400' : 'bg-red-500/10 text-red-400'
                          }`}>
                            {isPositive
                              ? <TrendingUp size={12} strokeWidth={3} />
                              : <TrendingDown size={12} strokeWidth={3} />
                            }
                            {isPositive ? '+' : ''}{stock.priceChangePercent?.toFixed(2) ?? '--'}%
                          </div>
                        </td>
                        <td className="px-6 py-4 text-right">
                          <span className="text-gray-400 text-sm font-mono">
                            {stock.volume?.toLocaleString() ?? '--'}
                          </span>
                        </td>
                        <td className="px-6 py-4 text-right">
                          <span className={`text-sm font-mono font-medium ${
                            stock.rsi > 70
                              ? 'text-red-400 drop-shadow-[0_0_8px_rgba(248,113,113,0.5)]'
                              : stock.rsi < 30
                              ? 'text-green-400 drop-shadow-[0_0_8px_rgba(52,211,153,0.5)]'
                              : 'text-gray-400'
                          }`}>
                            {stock.rsi?.toFixed(1) ?? '--'}
                          </span>
                        </td>
                        <td className="px-6 py-4 text-right">
                          <span className="text-gray-400 text-sm font-mono">
                            {stock.peRatio?.toFixed(1) ?? '--'}
                          </span>
                        </td>
                        <td className="px-6 py-4 text-center">
                          <ChevronRight
                            size={16}
                            className="text-gray-600 group-hover:text-green-500 transition-colors inline-block"
                          />
                        </td>
                      </tr>
                    )
                  })}
                </tbody>
              </table>
            </div>
          </div>
        )}
      </div>

      {selectedStock && (
        <StockDetailModal
          stock={selectedStock}
          onClose={() => setSelectedStock(null)}
        />
      )}
    </>
  )
}

export default StockList