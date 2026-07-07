import { useEffect, useRef, useState } from 'react'
import {
  AreaChart, Area, XAxis, YAxis, Tooltip,
  ResponsiveContainer, CartesianGrid
} from 'recharts'
import { X, TrendingUp, TrendingDown, Activity } from 'lucide-react'
import { getStockHistory } from '../../services/api'

const MAX_TICKS = 60 // keep last 60 price points

function StockDetailModal({ stock, onClose }) {
  const [priceHistory, setPriceHistory] = useState([])
  const prevPrice = useRef(null)

  // Seed chart with real DB history on open
  useEffect(() => {
    if (!stock?.symbol) return
    getStockHistory(stock.symbol, 1)
      .then(res => {
        const historicalPoints = res.data.map(pt => ({
          time: new Date(pt.timestamp).toLocaleTimeString('en-US', {
            hour: '2-digit', minute: '2-digit', second: '2-digit'
          }),
          price: parseFloat(pt.price.toFixed(2))
        }))
        setPriceHistory(historicalPoints.slice(-MAX_TICKS))
      })
      .catch(() => {/* ignore — will still receive live ticks */})
  }, [stock?.symbol])

  // Append new live ticks from WebSocket
  useEffect(() => {
    if (!stock?.currentPrice) return
    const price = stock.currentPrice
    const now = new Date()
    const label = now.toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit', second: '2-digit' })

    setPriceHistory(prev => {
      const next = [...prev, { time: label, price: parseFloat(price.toFixed(2)) }]
      return next.length > MAX_TICKS ? next.slice(next.length - MAX_TICKS) : next
    })
    prevPrice.current = price
  }, [stock?.currentPrice])

  if (!stock) return null

  const isPositive = (stock.priceChange ?? 0) >= 0
  const color = isPositive ? '#22c55e' : '#ef4444'
  const gradientId = `grad-${stock.symbol}`

  const minPrice = priceHistory.length > 1
    ? Math.min(...priceHistory.map(d => d.price))
    : (stock.low ?? stock.currentPrice ?? 0)
  const maxPrice = priceHistory.length > 1
    ? Math.max(...priceHistory.map(d => d.price))
    : (stock.high ?? stock.currentPrice ?? 100)
  const padding = (maxPrice - minPrice) * 0.1 || 1

  const stats = [
    { label: 'Price',   value: `$${stock.currentPrice?.toFixed(2) ?? '--'}` },
    { label: 'Change',  value: `${isPositive ? '+' : ''}${stock.priceChange?.toFixed(2) ?? '--'} (${stock.priceChangePercent?.toFixed(2) ?? '--'}%)`, color },
    { label: 'Volume',  value: stock.volume?.toLocaleString() ?? '--' },
    { label: 'RSI',     value: stock.rsi?.toFixed(1) ?? '--',
      color: stock.rsi > 70 ? '#ef4444' : stock.rsi < 30 ? '#22c55e' : '#d1d5db' },
    { label: 'P/E',     value: stock.peRatio?.toFixed(1) ?? '--' },
    { label: 'Open',    value: stock.open   ? `$${stock.open.toFixed(2)}`   : '--' },
    { label: 'High',    value: stock.high   ? `$${stock.high.toFixed(2)}`   : '--' },
    { label: 'Low',     value: stock.low    ? `$${stock.low.toFixed(2)}`    : '--' },
  ]

  return (
    /* Backdrop */
    <div
      className="fixed inset-0 z-50 flex items-end sm:items-center justify-center"
      style={{ background: 'rgba(0,0,0,0.6)', backdropFilter: 'blur(4px)' }}
      onClick={onClose}
    >
      {/* Panel */}
      <div
        className="relative w-full sm:max-w-2xl mx-4 mb-0 sm:mb-0 rounded-t-2xl sm:rounded-2xl overflow-hidden"
        style={{
          background: 'linear-gradient(145deg, #111827 0%, #0f172a 100%)',
          border: '1px solid #1f2937',
          animation: 'slideUp 0.25s ease-out',
        }}
        onClick={e => e.stopPropagation()}
      >
        {/* Header */}
        <div className="flex items-center justify-between px-6 pt-5 pb-4"
          style={{ borderBottom: '1px solid #1f2937' }}>
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-xl flex items-center justify-center font-bold text-sm"
              style={{ background: isPositive ? '#14532d' : '#450a0a', color }}>
              {stock.symbol.slice(0, 2)}
            </div>
            <div>
              <h2 className="text-white font-bold text-xl">{stock.symbol}</h2>
              <p className="text-gray-400 text-xs">Live price feed</p>
            </div>
          </div>
          <div className="flex items-center gap-4">
            <div className="text-right">
              <div className="text-white text-2xl font-bold">
                ${stock.currentPrice?.toFixed(2) ?? '--'}
              </div>
              <div className={`flex items-center justify-end gap-1 text-sm`} style={{ color }}>
                {isPositive ? <TrendingUp size={14} /> : <TrendingDown size={14} />}
                {isPositive ? '+' : ''}{stock.priceChangePercent?.toFixed(2) ?? '--'}%
              </div>
            </div>
            <button onClick={onClose}
              className="w-8 h-8 rounded-full flex items-center justify-center transition-colors"
              style={{ background: '#1f2937' }}>
              <X size={16} className="text-gray-400" />
            </button>
          </div>
        </div>

        {/* Chart */}
        <div className="px-4 pt-4 pb-2">
          {priceHistory.length < 2 ? (
            <div className="flex flex-col items-center justify-center h-36 gap-2">
              <Activity size={28} className="text-gray-600 animate-pulse" />
              <p className="text-gray-500 text-sm">Waiting for live ticks…</p>
            </div>
          ) : (
            <ResponsiveContainer width="100%" height={160}>
              <AreaChart data={priceHistory} margin={{ top: 4, right: 4, left: 0, bottom: 0 }}>
                <defs>
                  <linearGradient id={gradientId} x1="0" y1="0" x2="0" y2="1">
                    <stop offset="5%"  stopColor={color} stopOpacity={0.3} />
                    <stop offset="95%" stopColor={color} stopOpacity={0} />
                  </linearGradient>
                </defs>
                <CartesianGrid strokeDasharray="3 3" stroke="#1f2937" />
                <XAxis
                  dataKey="time"
                  tick={{ fill: '#6b7280', fontSize: 10 }}
                  interval="preserveStartEnd"
                  tickLine={false}
                  axisLine={false}
                />
                <YAxis
                  domain={[minPrice - padding, maxPrice + padding]}
                  tick={{ fill: '#6b7280', fontSize: 10 }}
                  tickFormatter={v => `$${v.toFixed(0)}`}
                  tickLine={false}
                  axisLine={false}
                  width={52}
                />
                <Tooltip
                  contentStyle={{ background: '#111827', border: '1px solid #374151', borderRadius: 8 }}
                  labelStyle={{ color: '#9ca3af', fontSize: 11 }}
                  itemStyle={{ color, fontWeight: 600 }}
                  formatter={v => [`$${v.toFixed(2)}`, 'Price']}
                />
                <Area
                  type="monotone"
                  dataKey="price"
                  stroke={color}
                  strokeWidth={2}
                  fill={`url(#${gradientId})`}
                  dot={false}
                  activeDot={{ r: 4, fill: color }}
                  isAnimationActive={false}
                />
              </AreaChart>
            </ResponsiveContainer>
          )}
        </div>

        {/* Stats grid */}
        <div className="grid grid-cols-4 gap-px mx-4 mb-5 mt-2 rounded-xl overflow-hidden"
          style={{ background: '#1f2937' }}>
          {stats.map(({ label, value, color: c }) => (
            <div key={label} className="flex flex-col items-center py-3 px-2"
              style={{ background: '#111827' }}>
              <span className="text-gray-500 text-xs mb-1">{label}</span>
              <span className="font-semibold text-sm" style={{ color: c ?? '#f9fafb' }}>
                {value}
              </span>
            </div>
          ))}
        </div>

        {/* Tip */}
        <p className="text-center text-gray-600 text-xs pb-4">
          Chart builds from live WebSocket ticks · click outside to close
        </p>
      </div>

      <style>{`
        @keyframes slideUp {
          from { transform: translateY(40px); opacity: 0; }
          to   { transform: translateY(0);   opacity: 1; }
        }
      `}</style>
    </div>
  )
}

export default StockDetailModal
