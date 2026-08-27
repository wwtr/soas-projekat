export default function Message({ text, type }) {
  if (!text) {
    return null
  }
  return <div className={`message ${type === 'error' ? 'error' : 'ok'}`}>{text}</div>
}
