import { createTheme } from "@mui/material/styles";

export const appTheme = createTheme({
  palette: {
    primary: {
      main: "#b45a38",
      dark: "#8d452a"
    },
    secondary: {
      main: "#355f59"
    },
    background: {
      default: "#f3eadf",
      paper: "#fff8f0"
    },
    text: {
      primary: "#201611",
      secondary: "#685b50"
    }
  },
  shape: {
    borderRadius: 22
  },
  typography: {
    fontFamily: "var(--font-body), Segoe UI, sans-serif",
    h1: {
      fontFamily: "var(--font-display), Georgia, serif",
      fontSize: "clamp(2.8rem, 7vw, 5.8rem)",
      fontWeight: 700,
      lineHeight: 0.98
    },
    h2: {
      fontFamily: "var(--font-display), Georgia, serif",
      fontSize: "clamp(2.3rem, 5.4vw, 4.8rem)",
      fontWeight: 700,
      lineHeight: 1
    },
    h3: {
      fontFamily: "var(--font-display), Georgia, serif",
      fontSize: "clamp(1.8rem, 4vw, 3rem)",
      fontWeight: 700,
      lineHeight: 1.05
    },
    h4: {
      fontFamily: "var(--font-display), Georgia, serif",
      fontWeight: 700,
      lineHeight: 1.1
    },
    h6: {
      lineHeight: 1.45,
      fontWeight: 600
    },
    button: {
      fontWeight: 700,
      textTransform: "none",
      letterSpacing: "-0.01em"
    },
    body1: {
      lineHeight: 1.7
    }
  },
  components: {
    MuiButton: {
      styleOverrides: {
        root: {
          borderRadius: 999
        }
      }
    },
    MuiPaper: {
      styleOverrides: {
        root: {
          backgroundImage: "none"
        }
      }
    }
  }
});
