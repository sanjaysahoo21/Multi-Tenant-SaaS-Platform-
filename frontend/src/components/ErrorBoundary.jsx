import { Component } from 'react';

class ErrorBoundary extends Component {
  constructor(props) {
    super(props);
    this.state = { hasError: false, error: null };
  }

  static getDerivedStateFromError(error) {
    return { hasError: true, error };
  }

  componentDidCatch(error, errorInfo) {
    console.error('Error caught by boundary:', error, errorInfo);
  }

  render() {
    if (this.state.hasError) {
      return (
        <div
          style={{
            padding: '20px',
            textAlign: 'center',
            color: 'var(--danger)',
            background: 'var(--surface)',
            border: '1px solid var(--border)',
            borderRadius: '8px',
            boxShadow: 'var(--shadow)',
            margin: '20px'
          }}
        >
          <h2 style={{ color: 'var(--text-primary)', marginBottom: '8px' }}>Something went wrong</h2>
          <p style={{ color: 'var(--text-secondary)' }}>{this.state.error?.message}</p>
          <button
            style={{
              marginTop: '12px',
              padding: '10px 16px',
              background: 'var(--primary)',
              color: 'var(--primary-contrast)',
              border: '1px solid var(--primary)',
              borderRadius: '8px',
              cursor: 'pointer',
              fontWeight: 600,
              boxShadow: '0 4px 12px rgba(0,0,0,0.12)'
            }}
            onClick={() => window.location.href = '/dashboard'}
          >
            Return to Dashboard
          </button>
        </div>
      );
    }

    return this.props.children;
  }
}

export default ErrorBoundary;
